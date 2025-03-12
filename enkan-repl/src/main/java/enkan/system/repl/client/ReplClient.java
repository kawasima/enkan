package enkan.system.repl.client;

import enkan.system.ReplResponse;
import enkan.system.repl.serdes.Fressian;
import enkan.system.repl.serdes.ReplResponseReader;
import enkan.system.repl.serdes.ReplResponseWriter;
import enkan.system.repl.serdes.ResponseStatusReader;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.zeromq.*;
import zmq.ZError;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.charset.Charset;

import static enkan.system.ReplResponse.ResponseStatus.DONE;
import static enkan.system.ReplResponse.ResponseStatus.SHUTDOWN;

/**
 * @author kawasima
 */
public class ReplClient {
    private final ExecutorService clientThread = Executors.newSingleThreadExecutor();
    private ConsoleHandler consoleHandler;

    private static class ConsoleHandler implements Runnable {
        private static final String MONITOR_ADDRESS = "inproc://monitor-";
        private ZContext ctx;
        private ZMQ.Socket socket;
        private ZMQ.Socket rendererSock;
        private ZMQ.Socket completerSock;
        private final LineReader reader;
        private final Fressian fressian;
        private final AtomicBoolean isAvailable = new AtomicBoolean(true);

        public ConsoleHandler(LineReader reader) {
            this.reader = reader;
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
            String monitorAddress = MONITOR_ADDRESS + UUID.randomUUID();
            socket = ctx.createSocket(SocketType.DEALER);
            final AtomicBoolean isSocketClosed = new AtomicBoolean(false);
            if (!socket.monitor(monitorAddress, ZMQ.EVENT_ALL)) {
                System.err.println("Monitoring failed");
            }

            final ZMQ.Socket monitorSocket = ctx.createSocket(SocketType.PAIR);
            monitorSocket.connect(monitorAddress);
            ZThread.fork(ctx, (args, c, pipe) -> {
                int retryCnt = 0;
                boolean active = true;
                while(active) {
                    ZEvent event = ZEvent.recv(monitorSocket, ZMQ.DONTWAIT);
                    if (event == null || monitorSocket.errno() == ZError.ETERM) {
                        break;
                    }
                    ZMonitor.Event eventType = event.getEvent();
                    if (eventType == ZMonitor.Event.DISCONNECTED || eventType == ZMonitor.Event.CLOSED) {
                        close();
                        active = false;
                        isSocketClosed.compareAndSet(false, true);
                    } else if (eventType == ZMonitor.Event.CONNECT_RETRIED) {
                        if (retryCnt++ > 3) {
                            System.err.println("Connection failed");
                            active = false;
                            isSocketClosed.compareAndSet(false, true);
                        }
                    }
                }
            });

            socket.connect("tcp://" + host + ":" + port);
            final ZMQ.Poller poller = ctx.createPoller(1);
            poller.register(socket, ZMQ.Poller.POLLIN);
            socket.send("/completer");

            ZMsg completerMsg = null;
            while (!Thread.currentThread().isInterrupted()) {
                if (isSocketClosed.get()) {
                    socket.close();
                    socket = null;
                    monitorSocket.close();
                    poller.close();
                    return;
                }

                poller.poll(1000);
                if (poller.pollin(0)) {
                     completerMsg = ZMsg.recvMsg(socket, false);
                     break;
                }
            }
            assert completerMsg != null;
            ReplResponse completerRes = fressian.read(completerMsg.pop().getData(), ReplResponse.class);
            String completerPort = completerRes.getOut();
            if (completerPort != null && completerPort.matches("\\d+")) {
                completerSock = ctx.createSocket(SocketType.DEALER);
                completerSock.connect("tcp://" + host + ":" + Integer.parseInt(completerPort));
                if (reader instanceof org.jline.reader.impl.LineReaderImpl) {
                    RemoteCompleter completer = new RemoteCompleter(completerSock);
                    ((org.jline.reader.impl.LineReaderImpl) reader).setCompleter(completer);
                } else {
                    System.err.println("Reader is not an instance of LineReaderImpl: " + reader.getClass());
                }
            }
            reader.getTerminal().writer().println("Connected to server (port = " + port +")");
            reader.getTerminal().writer().flush();

            rendererSock = ZThread.fork(ctx, (args, c, pipe) -> {
                while (socket != null) {
                    try {
                        ZMsg msg = ZMsg.recvMsg(this.socket);
                        ReplResponse res = fressian.read(msg.pop().getData(), ReplResponse.class);
                        if (res.getOut() != null) {
                            reader.getTerminal().writer().println(res.getOut());
                        } else if (res.getErr() != null) {
                            reader.getTerminal().writer().println(res.getErr());
                        }
                        if (res.getStatus().contains(SHUTDOWN)) {
                            reader.getTerminal().writer().flush();
                            pipe.send("shutdown");
                            break;
                        } else if (res.getStatus().contains(DONE)) {
                            pipe.send("done");
                        }
                        reader.getTerminal().writer().flush();
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
                    String line = reader.readLine("enkan> ");
                    if (line == null) continue;
                    line = line.trim();
                    if (line.startsWith("/connect ")) {
                        String[] arguments = line.split("\\s+");
                        if (arguments.length == 2 && arguments[1].matches("\\d+")) {
                            int port = Integer.parseInt(arguments[1]);
                            connect(port);
                        } else if (arguments.length > 2 && arguments[2].matches("\\d+")) {
                            String host = arguments[1];
                            int port = Integer.parseInt(arguments[2]);
                            connect(host, port);
                        } else {
                            reader.getTerminal().writer().println("/connect [host] port");
                        }
                    } else if (line.equals("/exit")) {
                        if (rendererSock != null) {
                            rendererSock.close();
                        }
                        close();
                        isAvailable.set(false);
                        return;
                    } else {
                        if (this.socket == null) {
                            reader.getTerminal().writer().println("Unconnected to enkan system.");
                        } else {
                            ((DefaultHistory) reader.getHistory()).save();
                            this.socket.send(line);
                            String serverInstruction = rendererSock.recvStr();
                            if (Objects.equals(serverInstruction, "shutdown")) {
                                close();
                                break;
                            }
                        }
                    }
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZError.ETERM) {
                        break;
                    }
                } catch (ZError.CtxTerminatedException e) {
                    System.err.println("disconnected");
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void close() {
            isAvailable.set(false);

            if (completerSock != null) {
                try {
                    completerSock.close();
                    completerSock = null;
                } catch (Exception ignore) {
                }
            }
            if (rendererSock != null) {
                try {
                    rendererSock.close();
                    rendererSock = null;
                } catch (Exception ignore) {
                }
            }
            if (socket != null) {
                socket.send("/disconnect", ZMQ.DONTWAIT);
                socket.close();
                socket = null;
            }

            if (ctx != null) {
                ctx.close();
                ctx = null;
            }
        }
    }

    public void start(int initialPort) throws Exception {
        try {
            // Enable debug logging
            System.setProperty("org.jline.terminal.debug", "true");
            System.setProperty("org.jline.terminal.dumb", "false");
            System.setProperty("org.jline.reader.debug", "true");
            
            // Try to create a native terminal
            Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .signalHandler(Terminal.SignalHandler.SIG_DFL)
                    .streams(System.in, System.out)
                    .encoding(Charset.defaultCharset())
                    .build();

            terminal.handle(Terminal.Signal.INT, signal -> {
                System.out.println("\nReceived SIGINT - shutting down...");
                close();
                System.exit(0);
            });

            // Configure terminal attributes
            terminal.enterRawMode();
            Attributes attrs = terminal.getAttributes();
            attrs.setLocalFlag(Attributes.LocalFlag.ECHO, false);
            terminal.setAttributes(attrs);

            // Configure parser
            DefaultParser parser = new DefaultParser();
            parser.setEscapeChars(null);

            // Configure line reader
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(parser)
                    .option(LineReader.Option.DISABLE_EVENT_EXPANSION, false)
                    .option(LineReader.Option.AUTO_FRESH_LINE, true)
                    .option(LineReader.Option.COMPLETE_IN_WORD, true)
                    .option(LineReader.Option.AUTO_MENU, true)
                    .build();

            reader.setVariable(LineReader.SECONDARY_PROMPT_PATTERN, "");
            reader.setVariable(LineReader.HISTORY_FILE, new File(System.getProperty("user.home"), ".enkan_history"));

            // Debug output
            System.out.println("LineReader class: " + reader.getClass().getName());
            System.out.println("Parser class: " + reader.getParser().getClass().getName());
            System.out.println("Completer status: " + (reader.getClass().getName().contains("LineReaderImpl") ? "Available" : "Not available"));

            consoleHandler = new ConsoleHandler(reader);
            if (initialPort > 0) {
                consoleHandler.connect(initialPort);
            }
            clientThread.execute(consoleHandler);
            clientThread.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        Runtime.getRuntime().addShutdownHook(new Thread(client::close));
        client.start();
    }
}
