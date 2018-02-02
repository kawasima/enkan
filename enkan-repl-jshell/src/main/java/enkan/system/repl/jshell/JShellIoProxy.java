package enkan.system.repl.jshell;

import enkan.system.ReplResponse;
import enkan.system.repl.ZmqServerTransport;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static enkan.system.ReplResponse.ResponseStatus.*;

public class JShellIoProxy {
    private PrintStream out;
    private PrintStream err;
    private BufferedReader outReader;
    private BufferedReader errReader;
    private ExecutorService ioThreadPool = Executors.newFixedThreadPool(2);

    public JShellIoProxy() {
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

    public void listen(ZmqServerTransport transport) {
        ioThreadPool.submit(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                if (transport.isClosed()) return;
                try {
                    String line = outReader.readLine();
                    if (Objects.equals(SystemIoTransport.CHUNK_DELEMETER, line)) {
                        transport.sendOut("");
                    } else {
                        transport.send(ReplResponse.withOut(line));
                    }
                } catch (IOException e) {
                    transport.sendErr(e.getMessage(), DONE);
                }
            }
        });

        ioThreadPool.submit(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                if (transport.isClosed()) return;
                try {
                    transport.send(ReplResponse.withErr(errReader.readLine()));
                } catch (IOException e) {
                    transport.sendErr(e.getMessage(), DONE);
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
