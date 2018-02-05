package enkan.system.repl.client;

import enkan.system.ReplResponse;
import enkan.system.repl.serdes.Fressian;
import enkan.system.repl.serdes.ReplResponseReader;
import enkan.system.repl.serdes.ReplResponseWriter;
import enkan.system.repl.serdes.ResponseStatusReader;
import jline.console.ConsoleReader;
import jline.console.completer.CandidateListCompletionHandler;
import jline.console.completer.StringsCompleter;
import jline.console.history.FileHistory;
import jline.console.history.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;
import zmq.ZError;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static enkan.system.ReplResponse.ResponseStatus.*;

/**
 * @author kawasima
 */
public class ReplClient implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ReplClient.class);

    ExecutorService clientThread = Executors.newSingleThreadExecutor();
    ConsoleReader console;
    ConsoleHandler consoleHandler;

    private static class ConsoleHandler implements Runnable {
        private ZContext ctx;
        private ZMQ.Socket socket;
        private ZMQ.Socket rendererSock;
        private ZMQ.Socket completerSock;
        private ConsoleReader console;
        private Fressian fressian;
        private AtomicBoolean isAvailable = new AtomicBoolean(true);

        public ConsoleHandler(ConsoleReader console) {
            this.console = console;
            this.ctx = new ZContext();
            this.fressian = new Fressian();
            fressian.putReadHandler(ReplResponse.class, new ReplResponseReader());
            fressian.putReadHandler(ReplResponse.ResponseStatus.class, new ResponseStatusReader());
            fressian.putWriteHandler(ReplResponse.class, new ReplResponseWriter());
            fressian.putWriteHandler(ReplResponse.ResponseStatus.class, new ReplResponseWriter());
        }

        public void connect(int port) throws IOException {
            connect("localhost", port);
        }

        public void connect(String host, int port) throws IOException {
            socket = ctx.createSocket(ZMQ.DEALER);
            socket.connect("tcp://" + host + ":" + port);
            socket.send("/completer");
            ZMsg completerMsg = ZMsg.recvMsg(socket);
            ReplResponse completerRes = fressian.read(completerMsg.pop().getData(), ReplResponse.class);
            String completerPort = completerRes.getOut();
            if (completerPort != null && completerPort.matches("\\d+")) {
                completerSock = ctx.createSocket(ZMQ.DEALER);
                completerSock.connect("tcp://" + host + ":" + Integer.parseInt(completerPort));
                console.addCompleter(new RemoteCompleter(completerSock));
            }
            console.println("Connected to server (port = " + port +")");
            console.flush();

            rendererSock = ZThread.fork(ctx, (args, c, pipe) -> {
                while (true) {
                    try {
                        ZMsg msg = ZMsg.recvMsg(this.socket);
                        ReplResponse res = fressian.read(msg.pop().getData(), ReplResponse.class);
                        if (res.getOut() != null) {
                            console.println(res.getOut());
                        } else if (res.getErr() != null) {
                            console.println(res.getErr());
                        }
                        if (res.getStatus().contains(SHUTDOWN)) {
                            console.flush();
                            pipe.send("shutdown");
                            break;
                        } else if (res.getStatus().contains(DONE)) {
                            pipe.send("done");
                        }
                        console.flush();
                    } catch (ZMQException e) {
                        if (e.getErrorCode() == ZError.ETERM) {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void run() {
            while(isAvailable.get()) {
                try {
                    String line = console.readLine();
                    if (line == null) continue;
                    line = line.trim();
                    if (line.startsWith("/connect ")) {
                        String[] arguments = line.split("\\s+");
                        if (arguments.length == 2 && arguments[1].matches("\\d+")) {
                            int port = Integer.parseInt(arguments[1]);
                            connect(port);
                        } else if (arguments.length > 2 && arguments[2].matches("\\d+")){
                            String host = arguments[1];
                            int port = Integer.parseInt(arguments[2]);
                            connect(host, port);
                        } else {
                            console.println("/connect [host] port");
                        }
                    } else if (line.equals("/exit")) {
                        rendererSock.close();
                        close();
                        return;
                    } else {
                        if (this.socket == null) {
                            console.println("Unconnected to enkan system.");
                        } else {
                            ((FileHistory) console.getHistory()).flush();
                            this.socket.send(line);
                            String serverInstruction = rendererSock.recvStr();
                            if (Objects.equals(serverInstruction, "shutdown")) {
                                close();
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

        public void close() {
            if (socket != null) {
                socket.send("/disconnect");
                socket.close();
            }

            if (ctx != null) {
                ctx.close();
                ctx = null;
            }
            isAvailable.set(false);
        }
    }

    public void start(int initialPort) throws Exception {
        console = new ConsoleReader();
        console.getTerminal().setEchoEnabled(false);
        console.setPrompt("\u001B[32menkan\u001B[0m> ");
        History history = new FileHistory(new File(System.getProperty("user.home"), ".enkan_history"));
        console.setHistory(history);

        CandidateListCompletionHandler handler = new CandidateListCompletionHandler();
        console.setCompletionHandler(handler);

        consoleHandler = new ConsoleHandler(console);
        if (initialPort > 0) {
            consoleHandler.connect(initialPort);
        }
        clientThread.execute(consoleHandler);
        clientThread.shutdown();
    }

    public void start() throws Exception {
        start(-1);
    }

    public void close() {
        consoleHandler.close();
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
        final ReplClient client = new ReplClient();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.close();
        }));
        client.start();
    }
}
