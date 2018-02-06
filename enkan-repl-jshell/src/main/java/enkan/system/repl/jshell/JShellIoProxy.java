package enkan.system.repl.jshell;

import enkan.system.ReplResponse;
import enkan.system.repl.ZmqServerTransport;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JShellIoProxy {
    private PrintStream out;
    private PrintStream err;
    private BufferedReader outReader;
    private BufferedReader errReader;
    private final Map<ZFrame, ZmqServerTransport> transports;
    private final ExecutorService ioThreadPool = Executors.newFixedThreadPool(2);

    public JShellIoProxy() {
        transports = new HashMap<>();
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
                    if (Objects.equals(SystemIoTransport.CHUNK_DELIMITER, line)) {
                        transports.values()
                                .forEach(t -> t.sendOut(""));
                    } else {
                        transports.values()
                                .forEach(t -> t.send(ReplResponse.withOut(line)));
                    }
                } catch (IOException e) {
                    transports.values()
                            .forEach(t-> t.sendErr(e.getMessage()));
                }
            }
        });

        ioThreadPool.submit(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    final ReplResponse response = ReplResponse.withErr(errReader.readLine());
                    if (!errReader.ready()) {
                        response.done();
                    }
                    transports.values()
                            .forEach(t -> t.send(response));
                } catch (IOException e) {
                    transports.values()
                            .forEach(t-> t.sendErr(e.getMessage()));
                }
            }
        });
    }

    public void stop() {
        ioThreadPool.shutdown();
        try {
            if (out != null)
                out.close();

            if (err != null)
                err.close();

            if (outReader != null)
                outReader.close();

            if (errReader != null)
                errReader.close();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
