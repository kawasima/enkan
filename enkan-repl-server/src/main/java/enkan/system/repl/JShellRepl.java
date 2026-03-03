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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static enkan.system.ReplResponse.ResponseStatus.DONE;
import static enkan.system.ReplResponse.ResponseStatus.SHUTDOWN;

/**
 * The REPL (Read-Eval-Print Loop) implementation using JShell.
 * @author kawasima
 */
public class JShellRepl implements Repl {
    private static final Logger LOG = LoggerFactory.getLogger(JShellRepl.class);

    private final JShell jshell;
    private final JShellIoProxy ioProxy;
    private final ExecutorService threadPool;
    private final Map<String, SystemCommand> localCommands = new HashMap<>();
    private final Set<String> commandNames = new HashSet<>();
    private final Map<String, Future<?>> backgroundTasks = new ConcurrentHashMap<>();
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
                    LOG.info("NONEXISTENT:{}", statement);
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
                    LOG.warn("{}:{}", event.status(), statement);
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
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) cl).getURLs();
                Arrays.stream(urls)
                        .map(URL::toString)
                        .forEach(jshell::addToClasspath);
            } else {
                String[] paths = System.getProperty("java.class.path").split(":");
                Arrays.stream(paths).forEach(jshell::addToClasspath);
            }
            // Add target/classes to classpath
            String userDir = System.getProperty("user.dir");
            jshell.addToClasspath(userDir + "/target/classes");
            executeStatement("import java.util.*");
            executeStatement("import enkan.system.*");
            executeStatement("import enkan.config.EnkanSystemFactory");
            executeStatement("import enkan.system.repl.jshell.JShellObjectTransferer");
            executeStatement("import enkan.system.repl.jshell.SystemIoTransport");
            executeStatement("var system = ((Class<? extends EnkanSystemFactory>)Class.forName(\"" + enkanSystemFactoryClassName + "\")).getConstructor().newInstance().create()");
            executeStatement("var transport = new SystemIoTransport()");
            executeStatement("var __commands = new HashMap<String, SystemCommand>()");
            // Import component classes discovered from the system created inside JShell
            executeStatement("system.getAllComponents().forEach(c -> System.out.println(c.getClass().getName()))");

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
            // Use the factory to discover component class names without creating a second live system
            EnkanSystemFactory factory = ((Class<? extends EnkanSystemFactory>) Class.forName(enkanSystemFactoryClassName))
                    .getConstructor().newInstance();
            factory.create().getAllComponents()
                    .forEach(c -> executeStatement("import " + c.getClass().getName()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eval(String statement, Transport transport) {
        JShellMessage msg = executeStatement(statement);
        msg.errs.forEach(line -> transport.send(ReplResponse.withErr(line)));
        msg.outs.forEach(line -> transport.send(ReplResponse.withOut(line)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerLocalCommand(String name, SystemCommand command) {
        localCommands.put(name, command);
        commandNames.add(name);
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
        Future<?> current = getBackground(name);
        if (current != null) {
            return;
        }
        Future<?> future = threadPool.submit(() -> {
            try {
                task.run();
            } finally {
                backgroundTasks.remove(name);
            }
        });
        backgroundTasks.put(name, future);
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
        Future<?> future = backgroundTasks.get(name);
        if (future != null && (future.isCancelled() || future.isDone())) {
            backgroundTasks.remove(name);
            return null;
        }
        return future;
    }

    /**
     * Escapes a string literal for safe embedding inside a JShell double-quoted string expression.
     * Handles backslashes, double-quotes, and control characters (newline, carriage-return, tab).
     */
    private static String escapeForJShellString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private void executeCommand(String commandName, String[] args, Transport transport) {
        try {
            String argStr = Arrays.stream(args)
                    .map(arg -> "\"" + escapeForJShellString(arg) + "\"")
                    .collect(Collectors.joining(","));
            StringBuilder execStatement = new StringBuilder("__commands.get(\"")
                    .append(escapeForJShellString(commandName))
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

        try (ZMQ.Socket server = ctx.createSocket(SocketType.ROUTER);
             ZMQ.Socket completerSock = ctx.createSocket(SocketType.ROUTER)) {
            int port = Env.getInt("repl.port", 0);
            String host = Env.getString("repl.host", "localhost");
            if (port == 0) {
                port = server.bindToRandomPort("tcp://" + host);
            } else {
                server.bind("tcp://" + host + ":" + port);
            }
            ioProxy.start();
            LOG.info("Listen {}", port);
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
                        command.execute(null, transport, Arrays.copyOfRange(cmds, 1, cmds.length));
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
            backgroundTasks.values().forEach(task -> task.cancel(true));
            threadPool.shutdownNow();
            jshell.close();
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
