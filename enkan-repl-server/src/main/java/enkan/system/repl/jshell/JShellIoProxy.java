package enkan.system.repl.jshell;

import enkan.system.ReplResponse;
import enkan.system.repl.ZmqServerTransport;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;

import java.io.*;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Proxies input/output operations for a JShell environment.
 *
 * <p>
 * This class manages communication between JShell and ZeroMQ transports,
 * redirecting standard output and error streams to connected clients.
 * </p>
 *
 * <p>
 * It uses piped streams to capture JShell output and redirects it to
 * appropriate ZeroMQ transports.
 * </p>
 *
 * @author kawasima
 */
public class JShellIoProxy {
    /** PrintStream for capturing standard output */
    private final PrintStream out;
    private final PrintStream err;
    private final BufferedReader outReader;
    private final BufferedReader errReader;
    private final Map<ZFrame, ZmqServerTransport> transports;
    private final ExecutorService ioThreadPool = Executors.newFixedThreadPool(2);

    public JShellIoProxy() {
        transports = new ConcurrentHashMap<>();
        PipedOutputStream outPipe = new PipedOutputStream();
        PipedOutputStream errPipe = new PipedOutputStream();
        try {
            outReader = new BufferedReader(new InputStreamReader(new PipedInputStream(outPipe)));
            out = new PrintStream(outPipe);
            errReader = new BufferedReader(new InputStreamReader(new PipedInputStream(errPipe)));
            err = new PrintStream(errPipe);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public PrintStream forJShellPrintStream() {
        return out;
    }

    public PrintStream forJShellErrorStream() {
        return err;
    }

    public ZmqServerTransport listen(ZMQ.Socket socket, ZFrame clientAddress) {
        return transports.computeIfAbsent(clientAddress,
                addr -> new ZmqServerTransport(socket, addr));
    }

    public void unlisten(ZFrame clientAddress) {
        ZmqServerTransport transport = transports.remove(clientAddress);
        transport.close();
    }

    public void start() {
        ioThreadPool.submit(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    String line = outReader.readLine();
                    // null means the pipe was closed; exit the reader loop
                    if (line == null) break;
                    if (Objects.equals(SystemIoTransport.CHUNK_DELIMITER, line)) {
                        Thread.sleep(200);
                        transports.values()
                                .forEach(t -> t.sendOut(""));
                    } else {
                        transports.values()
                                .forEach(t -> t.send(ReplResponse.withOut(line)));
                    }
                } catch (IOException e) {
                    transports.values()
                            .forEach(t-> t.sendErr(e.getMessage()));
                    break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        ioThreadPool.submit(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    String line = errReader.readLine();
                    // null means the pipe was closed; exit the reader loop
                    if (line == null) break;
                    transports.values()
                            .forEach(t -> t.send(ReplResponse.withErr(line)));
                } catch (IOException e) {
                    transports.values()
                            .forEach(t-> t.sendErr(e.getMessage()));
                    break;
                }
            }
        });
    }

    public void stop() {
        ioThreadPool.shutdown();
        try {
            out.close();
            err.close();
            outReader.close();
            errReader.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
