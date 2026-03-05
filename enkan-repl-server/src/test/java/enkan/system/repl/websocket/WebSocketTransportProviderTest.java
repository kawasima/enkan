package enkan.system.repl.websocket;

import enkan.system.repl.JShellRepl;
import enkan.system.repl.MinimalSystemFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that start a real {@link JShellRepl} with a
 * {@link WebSocketTransportProvider} and verify communication over WebSocket.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebSocketTransportProviderTest {
    private JShellRepl repl;
    private ExecutorService replThread;
    private int wsPort;

    @BeforeAll
    void startServer() {
        repl = new JShellRepl(MinimalSystemFactory.class.getName());
        WebSocketTransportProvider wsProvider = new WebSocketTransportProvider();
        repl.addTransportProvider(wsProvider);

        replThread = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "test-repl-ws");
            t.setDaemon(true);
            return t;
        });
        replThread.submit(repl);

        // Wait for REPL (and transport providers) to be ready
        repl.getPort();

        // Poll until the WebSocket server is accepting connections
        for (int i = 0; i < 50; i++) {
            wsPort = wsProvider.getPort();
            if (wsPort > 0) {
                try (Socket probe = new Socket("localhost", wsPort)) {
                    break;
                } catch (Exception ignored) {
                }
            }
            try { Thread.sleep(100); } catch (InterruptedException e) { break; }
        }
        assertThat(wsPort).as("WebSocket server did not start").isGreaterThan(0);
    }

    @AfterAll
    void stopServer() throws Exception {
        if (replThread != null) {
            replThread.shutdownNow();
            replThread.awaitTermination(3, TimeUnit.SECONDS);
        }
    }

    @Test
    void connectAndExecuteHelp() throws Exception {
        try (WsClient ws = new WsClient("localhost", wsPort)) {
            ws.send("/help");
            List<String> messages = ws.receiveUntilDone();
            assertThat(messages).isNotEmpty();
            // At least one message should contain a command name like /start or /help
            assertThat(messages.stream().anyMatch(m -> m.contains("start") || m.contains("help")))
                    .as("Expected help output to mention commands, got: %s", messages)
                    .isTrue();
        }
    }

    @Test
    void evalExpression() throws Exception {
        try (WsClient ws = new WsClient("localhost", wsPort)) {
            ws.send("1 + 1");
            List<String> messages = ws.receiveUntilDone();
            assertThat(messages.stream().anyMatch(m -> m.contains("2")))
                    .as("Expected '2' in response, got: %s", messages)
                    .isTrue();
        }
    }

    @Test
    void disconnectDoesNotCauseServerError() throws Exception {
        try (WsClient ws = new WsClient("localhost", wsPort)) {
            ws.send("/help");
            ws.receiveUntilDone();
        }
        // Connect again to verify server is still healthy
        try (WsClient ws = new WsClient("localhost", wsPort)) {
            ws.send("1 + 1");
            List<String> messages = ws.receiveUntilDone();
            assertThat(messages).isNotEmpty();
        }
    }

    /**
     * Minimal WebSocket client using raw TCP sockets.
     * Performs HTTP Upgrade handshake and reads/writes RFC 6455 frames.
     */
    static class WsClient implements Closeable {
        private static final String WS_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        private final Socket socket;
        private final InputStream in;
        private final OutputStream out;

        WsClient(String host, int port) throws Exception {
            socket = new Socket(host, port);
            socket.setSoTimeout(5000);
            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
            performHandshake(host, port);
        }

        private void performHandshake(String host, int port) throws Exception {
            String key = Base64.getEncoder().encodeToString("test-ws-key-1234".getBytes());
            String request = "GET /repl HTTP/1.1\r\n" +
                    "Host: " + host + ":" + port + "\r\n" +
                    "Upgrade: websocket\r\n" +
                    "Connection: Upgrade\r\n" +
                    "Sec-WebSocket-Key: " + key + "\r\n" +
                    "Sec-WebSocket-Version: 13\r\n" +
                    "\r\n";
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();

            // Read response headers
            String expectedAccept = computeAccept(key);
            StringBuilder headers = new StringBuilder();
            int b;
            while ((b = in.read()) != -1) {
                headers.append((char) b);
                if (headers.toString().endsWith("\r\n\r\n")) break;
            }
            String headerStr = headers.toString();
            assertThat(headerStr).startsWith("HTTP/1.1 101");
            assertThat(headerStr).contains(expectedAccept);
        }

        private static String computeAccept(String key) throws Exception {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest((key + WS_MAGIC).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        }

        void send(String text) throws IOException {
            byte[] payload = text.getBytes(StandardCharsets.UTF_8);
            byte[] mask = {0x12, 0x34, 0x56, 0x78};

            // Frame: FIN=1, opcode=1 (text)
            out.write(0x81);
            // Masked, payload length
            if (payload.length < 126) {
                out.write(0x80 | payload.length);
            } else {
                out.write(0x80 | 126);
                out.write((payload.length >> 8) & 0xFF);
                out.write(payload.length & 0xFF);
            }
            out.write(mask);
            for (int i = 0; i < payload.length; i++) {
                out.write(payload[i] ^ mask[i % 4]);
            }
            out.flush();
        }

        String readFrame() throws IOException {
            int first = in.read();
            if (first == -1) return null;
            int second = in.read();
            boolean masked = (second & 0x80) != 0;
            long len = second & 0x7F;
            if (len == 126) {
                len = ((in.read() & 0xFF) << 8) | (in.read() & 0xFF);
            } else if (len == 127) {
                len = 0;
                for (int i = 0; i < 8; i++) {
                    len = (len << 8) | (in.read() & 0xFF);
                }
            }
            byte[] maskKey = null;
            if (masked) {
                maskKey = new byte[4];
                readExact(in, maskKey);
            }
            byte[] data = new byte[(int) len];
            readExact(in, data);
            if (maskKey != null) {
                for (int i = 0; i < data.length; i++) {
                    data[i] ^= maskKey[i % 4];
                }
            }
            int opcode = first & 0x0F;
            if (opcode == 0x8) return null; // close frame
            return new String(data, StandardCharsets.UTF_8);
        }

        private static void readExact(InputStream in, byte[] buf) throws IOException {
            int off = 0;
            while (off < buf.length) {
                int n = in.read(buf, off, buf.length - off);
                if (n == -1) throw new EOFException();
                off += n;
            }
        }

        List<String> receiveUntilDone() throws IOException {
            List<String> messages = new ArrayList<>();
            while (true) {
                String frame = readFrame();
                if (frame == null) break;
                messages.add(frame);
                // Check if this frame contains a DONE status
                if (frame.contains("\"DONE\"")) break;
            }
            return messages;
        }

        @Override
        public void close() throws IOException {
            // Send close frame
            try {
                out.write(new byte[]{(byte) 0x88, (byte) 0x80, 0x00, 0x00, 0x00, 0x00});
                out.flush();
            } catch (IOException ignored) {
            }
            socket.close();
        }
    }
}
