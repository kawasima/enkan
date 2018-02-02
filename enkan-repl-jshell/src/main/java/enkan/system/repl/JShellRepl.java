package enkan.system.repl;

import enkan.config.EnkanSystemFactory;
import enkan.exception.FalteringEnvironmentException;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.ReplResponse;
import enkan.system.SystemCommand;
import enkan.system.command.*;
import enkan.system.repl.jshell.JShellIoProxy;
import enkan.system.repl.jshell.JShellObjectTransferer;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import lombok.Data;
import lombok.Value;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static enkan.system.ReplResponse.ResponseStatus.SHUTDOWN;

public class JShellRepl implements Repl {
    private static final Logger LOG = LoggerFactory.getLogger(JShellRepl.class);

    private JShell jshell;
    private JShellIoProxy ioProxy;
    private ExecutorService threadPool;
    private final Map<String, SystemCommand> commands = new HashMap<>();
    private final Set<String> commandNames = new HashSet<>();
    private final Map<String, Future<?>> backgroundTasks = new HashMap<>();
    private final CompletableFuture<Integer> replPort = new CompletableFuture<>();

    private JShellMessage executeStatement(String statement) {
        statement = statement.trim();
        if (statement.endsWith(";")) statement = statement.replaceAll("\\;+$", "");

        List<SnippetEvent> events = jshell.eval(statement + ";");
        JShellMessage msg = new JShellMessage();
        for (SnippetEvent event: events) {
            switch(event.status()) {
                case VALID:
                    if (event.exception() != null) {
                        StringWriter sw = new StringWriter();
                        event.exception().printStackTrace(new PrintWriter(sw));
                        Arrays.stream(sw.toString().split(System.lineSeparator()))
                                .forEach(line -> msg.errs.add(line));
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

    public JShellRepl(String enkanSystemFactoryClassName) {
        try {
            ioProxy = new JShellIoProxy();
            jshell = JShell.builder()
                    .out(ioProxy.forJShellPrintStream())
                    .err(ioProxy.forJShellErrorStream())
                    .build();
            String cp = System.getProperty("java.class.path");
            String[] elements = cp.split(File.pathSeparator);
            for (String path : elements) {
                jshell.addToClasspath(path);
            }
            executeStatement("import java.util.*");
            executeStatement("import enkan.system.*");
            executeStatement("import enkan.config.EnkanSystemFactory");
            executeStatement("import enkan.system.repl.jshell.JShellObjectTransferer");
            executeStatement("import enkan.system.repl.jshell.SystemIoTransport");
            executeStatement("SystemIoTransport transport = new SystemIoTransport()");
            executeStatement("EnkanSystem system = ((Class<? extends EnkanSystemFactory>)Class.forName(\"" + enkanSystemFactoryClassName + "\")).newInstance().create()");
            threadPool = Executors.newCachedThreadPool(runnable -> {
                Thread t = new Thread(runnable);
                t.setName("enkan-repl-pseudo");
                return t;
            });
            registerCommand("start", new StartCommand());
            registerCommand("stop",  new StopCommand());
            registerCommand("reset", new ResetCommand());
            registerCommand("help",  new HelpCommand(commandNames));
            registerCommand("middleware", new MiddlewareCommand());
            EnkanSystem system = ((Class<? extends EnkanSystemFactory>) Class.forName(enkanSystemFactoryClassName)).getConstructor().newInstance().create();
            system.getAllComponents().stream()
                    .forEach(c -> executeStatement("import " + c.getClass().getName()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

    }
    @Override
    public void registerCommand(String name, SystemCommand command) {
        try {
            String serializedCommand = JShellObjectTransferer.writeToBase64(command);
            executeStatement("SystemCommand " + name
                    + " = JShellObjectTransferer.readFromBase64(\""
                    + serializedCommand
                    + "\", SystemCommand.class)");
            commandNames.add(name);
        } catch (Exception e) {
            throw new IllegalArgumentException("command cannot be serialized:" + command, e);
        }
    }

    @Override
    public void addBackgroundTask(String name, Runnable task) {

    }

    @Override
    public Integer getPort() {
        try {
            return replPort.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FalteringEnvironmentException(e);
        }
    }

    @Override
    public Future<?> getBackgorundTask(String name) {
        return null;
    }

    protected void printHelp() {
        System.out.println("start - Start system\n" +
                "stop - Stop system.\n" +
                "reset - Reset system.\n" +
                "exit - exit repl.\n"
        );
    }

    @Override
    public void run() {
        ZContext ctx = new ZContext();
        try {
            ZMQ.Socket server = ctx.createSocket(ZMQ.ROUTER);
            int port = server.bindToRandomPort("tcp://localhost");

            LOG.info("Listen " + port);
            replPort.complete(port);

            while(!Thread.currentThread().isInterrupted()) {
                ZMsg msg = ZMsg.recvMsg(server);
                ZFrame clientAddress = msg.pop();
                ZmqServerTransport transport = new ZmqServerTransport(server, clientAddress);
                ioProxy.listen(transport);
                String[] cmd = msg.popString().trim().split("\\s+");
                if (cmd[0].startsWith("/")) {
                    String commandName = cmd[0].substring(1);
                    if (cmd[0].isEmpty()) {
                        printHelp();
                    } else if (Objects.equals("/shutdown", cmd[0])) {
                        executeStatement("shutdown.exec(system, transport)");
                        transport.sendOut("shutdown", SHUTDOWN);
                        break;
                    } else {
                        String[] args = new String[cmd.length - 1];
                        System.arraycopy(cmd, 1, args, 0, cmd.length - 1);
                        try {
                            String argStr = Arrays.stream(args)
                                    .map(arg -> "\"" + arg.replaceAll("\"", "\\\"")+ "\"")
                                    .collect(Collectors.joining(","));
                            StringBuilder execStatement = new StringBuilder(commandName)
                                    .append(".execute(system, transport");
                            if (!argStr.isEmpty()) {
                                execStatement.append(",").append(argStr);
                            }
                            execStatement.append(" )");

                            JShellMessage evalMessage = executeStatement(execStatement.toString());
                            if (!evalMessage.errs.isEmpty()) {
                                evalMessage.errs.forEach(line -> transport.send(ReplResponse.withErr(line)));
                                transport.sendOut("");
                            }
                        } catch (Throwable ex) {
                            StringWriter traceWriter = new StringWriter();
                            ex.printStackTrace(new PrintWriter(traceWriter));
                            transport.sendErr(traceWriter.toString());
                        }
                    }
                } else {
                    JShellMessage evalMessage = executeStatement(String.join(" ", cmd));
                    evalMessage.errs.forEach(line -> transport.send(ReplResponse.withErr(line)));
                    evalMessage.outs.forEach(line -> transport.send(ReplResponse.withOut(line)));
                    transport.send(ReplResponse.withOut("").done());
                }
            }
        } catch (Exception e) {
            LOG.error("Repl server error", e);
        } finally {
            ctx.close();
            try {
                threadPool.shutdown();
                if (!threadPool.awaitTermination(3L, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
            }
            ioProxy.stop();
            jshell.close();
        }

    }

    @Data
    private static class JShellMessage {
        private List<String> outs = new ArrayList<>();
        private List<String> errs = new ArrayList<>();
    }
}
