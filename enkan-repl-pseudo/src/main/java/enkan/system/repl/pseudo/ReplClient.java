package enkan.system.repl.pseudo;

import enkan.system.ReplResponse;
import jline.console.ConsoleReader;
import jline.console.completer.CandidateListCompletionHandler;
import jline.console.completer.StringsCompleter;
import jline.console.history.FileHistory;
import jline.console.history.History;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static enkan.system.ReplResponse.ResponseStatus.DONE;
import static enkan.system.ReplResponse.ResponseStatus.SHUTDOWN;

/**
 * @author kawasima
 */
public class ReplClient {

    ExecutorService clientThread = Executors.newFixedThreadPool(2);
    ConsoleReader console;
    ConsoleHandler consoleHandler;

    private static class ConsoleHandler implements Runnable {
        private Socket socket;
        private Packer packer;
        private Unpacker unpacker;
        private ConsoleReader console;
        private MessagePack msgpack = new MessagePack();
        private AtomicBoolean isAvailable = new AtomicBoolean(true);

        public ConsoleHandler(ConsoleReader console) {
            this.console = console;
        }

        public void connect(int port) throws IOException {
            socket = new Socket("localhost", port);
            packer = msgpack.createPacker(socket.getOutputStream());
            unpacker = msgpack.createUnpacker(socket.getInputStream());
            console.println("Connected to server (port = " + port +")");
        }

        @Override
        public void run() {
            msgpack.register(ReplResponse.ResponseStatus.class);
            msgpack.register(ReplResponse.class);

            while (isAvailable.get()) {
                try {
                    String line = console.readLine();
                    if (line == null) continue;
                    line = line.trim();

                    if (line.startsWith("/connect")) {
                        String[] arguments = line.split("\\s+");
                        if (arguments.length > 1) {
                            int port = Integer.parseInt(arguments[1]);
                            connect(port);
                        } else {
                            console.println("/connect [port]");
                        }
                    } else if (line.equals("/exit")) {
                        socket.close();
                        return;
                    } else {
                        if (socket == null) {
                            console.println("Unconnected to enkan system.");
                        } else {
                            packer.write(line);
                            while (true) {
                                ReplResponse res = unpacker.read(ReplResponse.class);
                                if (res.getOut() != null) {
                                    console.println(res.getOut());
                                } else if (res.getErr() != null) {
                                    console.println(res.getErr());
                                }
                                if (res.getStatus().contains(SHUTDOWN)) {
                                    console.flush();
                                    close();
                                    return;
                                } else if (res.getStatus().contains(DONE)) {
                                    break;
                                }
                            }
                            console.flush();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void close() throws IOException {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
            isAvailable.set(false);
        }
    }

    public void start() throws Exception {
        console = new ConsoleReader();
        console.getTerminal().setEchoEnabled(false);
        console.setPrompt("\u001B[32menkan\u001B[0m> ");
        History history = new FileHistory(new File(System.getProperty("user.home"), ".enkan_history"));
        console.setHistory(history);

        console.addCompleter(new StringsCompleter("/connect", "/exit"));
        CandidateListCompletionHandler handler = new CandidateListCompletionHandler();
        console.setCompletionHandler(handler);
        consoleHandler = new ConsoleHandler(console);
        clientThread.execute(consoleHandler);
    }

    public void start(int initialPort) throws Exception {
        start();
        consoleHandler.connect(initialPort);
    }

    public void close() {
        try {
            consoleHandler.close();
        } catch (IOException ignore) {

        }
        try {
            clientThread.shutdown();
            if (!clientThread.awaitTermination(1L, TimeUnit.SECONDS)) {
                clientThread.shutdownNow();
            }
        } catch (InterruptedException ex) {
            clientThread.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        ReplClient client = new ReplClient();
        client.start();
    }
}
