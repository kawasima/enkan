package enkan.system.repl;

import enkan.Env;
import enkan.config.EnkanSystemFactory;
import enkan.exception.FalteringEnvironmentException;
import enkan.system.*;
import enkan.system.command.*;
import enkan.system.repl.jshell.CompletionServer;
import enkan.system.repl.jshell.JShellIoProxy;
import enkan.system.repl.jshell.JShellObjectTransferer;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static enkan.system.ReplResponse.ResponseStatus.DONE;
import static enkan.system.ReplResponse.ResponseStatus.SHUTDOWN;

public class JShellRepl implements Repl {
    private static final Logger LOG = LoggerFactory.getLogger(JShellRepl.class);

    private JShell jshell;
    private JShellIoProxy ioProxy;
    private ExecutorService threadPool;
    private final Map<String, SystemCommand> localCommands = new HashMap<>();
    private final Set<String> commandNames = new HashSet<>();
    private final Map<String, Future<?>> backgroundTasks = new HashMap<>();
    private final CompletableFuture<Integer> replPort = new CompletableFuture<>();

    private JShellMessage executeStatement(String statement) {
        statement = statement.trim();
        if (statement.endsWith(";")) statement = statement.replaceAll(";+$", "");

        List<SnippetEvent> events = jshell.eval(statement + ";");
        JShellMessage msg = new JShellMessage();
        for (SnippetEvent event: events) {
            switch(event.status()) {
                case VALID:
                    if (event.exception() != null) {
                        StringWriter sw = new StringWriter();
                        event.exception().printStackTrace(new PrintWriter(sw));
                        msg.errs.addAll(Arrays.asList(sw.toString().split(System.lineSeparator())));
                    } else {
                        msg.outs.add(event.value());
                    }
                    break;
                case NONEXISTENT:
                    LOG.info("NONEXISTENT:" + statement);
                    break;
                case REJECTED:
                    jshell.diagnostics(event.snippet())
                            .forEach(diag -> {
                                if (diag.isError()) {
                                    msg.errs.add(diag.getMessage(Locale.getDefault()));
                                } else {
                                    msg.outs.add(diag.getMessage(Locale.getDefault()));
                                }
                            });
                    break;
                default:
                    LOG.warn(event.status() + ":" + statement);
            }
        }
        return msg;
    }

    @SuppressWarnings("unchecked")
    public JShellRepl(String enkanSystemFactoryClassName) {
        try {
            ioProxy = new JShellIoProxy();
            jshell = JShell.builder()
                    .out(ioProxy.forJShellPrintStream())
                    .err(ioProxy.forJShellErrorStream())
                    .build();
            String cp = System.getProperty("java.class.path");
            jshell.addToClasspath(cp);
            
            executeStatement("import java.util.*");
            executeStatement("import enkan.system.*");
            executeStatement("import enkan.config.EnkanSystemFactory");
            executeStatement("import enkan.system.repl.jshell.JShellObjectTransferer");
            executeStatement("import enkan.system.repl.jshell.SystemIoTransport");
            executeStatement("SystemIoTransport transport = new SystemIoTransport()");
            executeStatement("EnkanSystem system = ((Class<? extends EnkanSystemFactory>)Class.forName(\"" + enkanSystemFactoryClassName + "\")).newInstance().create()");
            executeStatement("Map<String, SystemCommand> __commands = new HashMap<>()");

            threadPool = Executors.newCachedThreadPool(runnable -> {
                Thread t = new Thread(runnable);
                t.setName("enkan-repl-jshell");
                return t;
            });
            registerCommand("start", new StartCommand());
            registerCommand("stop",  new StopCommand());
            registerCommand("reset", new ResetCommand());
            registerCommand("help",  new HelpCommand(commandNames));
            registerCommand("middleware", new MiddlewareCommand());
            EnkanSystem system = ((Class<? extends EnkanSystemFactory>) Class.forName(enkanSystemFactoryClassName)).getConstructor().newInstance().create();
            system.getAllComponents()
                    .forEach(c -> executeStatement("import " + c.getClass().getName()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerCommand(String name, SystemCommand command) {
        try {
            String serializedCommand = JShellObjectTransferer.writeToBase64(command);
            executeStatement("__commands.put(\"" + name
                    + "\", JShellObjectTransferer.readFromBase64(\""
                    + serializedCommand
                    + "\", SystemCommand.class))");
        } catch (Exception e) {
            throw new IllegalArgumentException("command cannot be serialized:" + command, e);
        }
        commandNames.add(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBackgroundTask(String name, Runnable task) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPort() {
        try {
            return replPort.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FalteringEnvironmentException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<?> getBackground(String name) {
        return null;
    }

    private void executeCommand(String commandName, String[] args, Transport transport) {
        try {
            String argStr = Arrays.stream(args)
                    .map(arg -> "\"" + arg.replaceAll("\"", "\\\"") + "\"")
                    .collect(Collectors.joining(","));
            StringBuilder execStatement = new StringBuilder("__commands.get(\"")
                    .append(commandName)
                    .append("\").execute(system, transport");
            if (!argStr.isEmpty()) {
                execStatement.append(",").append(argStr);
            }
            execStatement.append(")");

            JShellMessage evalMessage = executeStatement(execStatement.toString());
            if (!evalMessage.errs.isEmpty()) {
                evalMessage.errs.forEach(line -> transport.send(ReplResponse.withErr(line)));
                transport.sendErr("", DONE);
            }
        } catch (Throwable ex) {
            StringWriter traceWriter = new StringWriter();
            ex.printStackTrace(new PrintWriter(traceWriter));
            transport.sendErr(traceWriter.toString());
        }
    }

    @Override
    public void run() {
        ZContext ctx = new ZContext();
        ZMQ.Socket server = ctx.createSocket(SocketType.ROUTER);
        ZMQ.Socket completerSock = ctx.createSocket(SocketType.ROUTER);

        try {
            int port = Env.getInt("repl.port", 0);
            String host = Env.getString("repl.host", "localhost");
            if (port == 0) {
                port = server.bindToRandomPort("tcp://" + host);
            } else {
                server.bind("tcp://" + host + ":" + port);
            }
            ioProxy.start();
            LOG.info("Listen " + port);
            replPort.complete(port);

            // Completer
            int completerPort = Env.getInt("completer.port", 0);
            if (completerPort == 0) {
                completerPort = completerSock.bindToRandomPort("tcp://" + host);
            } else {
                completerSock.bind("tcp://" + host + ":" + completerPort);
            }
            threadPool.submit(new CompletionServer(completerSock, jshell.sourceCodeAnalysis(), commandNames));
            LOG.info("Completer start=tcp://{}:{}", host, completerPort);

            while (!Thread.currentThread().isInterrupted()) {
                ZMsg msg = ZMsg.recvMsg(server);
                ZFrame clientAddress = msg.pop();
                ZmqServerTransport transport = ioProxy.listen(server, clientAddress);

                String[] cmds = msg.popString().trim().split("\\s+");
                if (cmds[0].startsWith("/")) {
                    String commandName = cmds[0].substring(1);
                    if (commandName.isEmpty()) {
                        executeCommand("help", new String[0], transport);
                    } else if (Objects.equals("completer", commandName)) {
                        transport.sendOut(Integer.toString(completerPort));
                    } else if (Objects.equals("shutdown", commandName)) {
                        executeStatement("shutdown.exec(system, transport)");
                        transport.sendOut("shutdown", SHUTDOWN);
                        ioProxy.stop();
                        break;
                    } else if (Objects.equals("disconnect", commandName)) {
                        transport.sendOut("disconnected", DONE);
                        ioProxy.unlisten(clientAddress);
                    } else if (localCommands.containsKey(commandName)) {
                        SystemCommand command = localCommands.get(commandName);
                    } else {
                        String[] args = new String[cmds.length - 1];
                        System.arraycopy(cmds, 1, args, 0, cmds.length - 1);
                        executeCommand(commandName, args, transport);
                    }
                } else {
                    JShellMessage evalMessage = executeStatement(String.join(" ", cmds));
                    evalMessage.errs.forEach(line -> transport.send(ReplResponse.withErr(line)));
                    evalMessage.outs.forEach(line -> transport.send(ReplResponse.withOut(line)));
                    transport.send(ReplResponse.withOut("").done());
                }
            }
        } catch (Exception e) {
            LOG.error("REPL server error", e);
        } finally {
            LOG.info("Shutdown REPL server");
            try {
                threadPool.shutdown();
                if (!threadPool.awaitTermination(3L, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
            }
            jshell.close();
            ctx.destroySocket(completerSock);
            ctx.destroySocket(server);
            ctx.destroy();
        }

    }

    private static class JShellMessage implements Serializable {
        private final List<String> outs = new ArrayList<>();
        private final List<String> errs = new ArrayList<>();

        public List<String> getOuts() {
            return outs;
        }

        public List<String> getErrs() {
            return errs;
        }
    }
}
