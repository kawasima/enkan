package enkan.system.repl;

import enkan.Env;
import enkan.config.EnkanSystemFactory;
import enkan.exception.FalteringEnvironmentException;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.ReplResponse;
import enkan.system.SystemCommand;
import enkan.system.command.*;
import enkan.system.repl.pseudo.CompletionServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static enkan.system.ReplResponse.ResponseStatus.SHUTDOWN;
import static enkan.util.ReflectionUtils.tryReflection;

/**
 * @author kawasima
 */
public class PseudoRepl implements Repl {
    private final Logger LOG = LoggerFactory.getLogger(PseudoRepl.class);

    private final EnkanSystem system;
    private final ExecutorService threadPool;
    private final Set<String> commandNames = new HashSet<>();
    private final Map<String, SystemCommand> commands = new HashMap<>();
    private final Map<String, Future<?>> backgroundTasks = new HashMap<>();
    private final CompletableFuture<Integer> replPort = new CompletableFuture<>();
    
    @SuppressWarnings("unchecked")
    public PseudoRepl(String enkanSystemFactoryClassName) {
        system = tryReflection(() -> ((Class<? extends EnkanSystemFactory>) Class.forName(enkanSystemFactoryClassName))
                    .getConstructor().newInstance().create());

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
    }

    protected void printHelp() {
        System.out.println("start - Start system\n" +
                "stop - Stop system.\n" +
                "reset - Reset system.\n" +
                "exit - exit repl.\n"
        );
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
    public void registerCommand(String name, SystemCommand command) {
        commandNames.add(name);
        commands.put(name, command);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("pseudo-repl-server");
        ZContext ctx = new ZContext();
        try (ZMQ.Socket server = ctx.createSocket(SocketType.ROUTER);
             ZMQ.Socket completerSock = ctx.createSocket(SocketType.ROUTER)){
            int port = Env.getInt("repl.port", 0);
            String host = Env.getString("repl.host", "localhost");
            if (port == 0) {
                port = server.bindToRandomPort("tcp://" + host);
            } else {
                server.bind("tcp://" + host + ":" + port);
            }

            registerCommand("shutdown", (system, transport, args) -> {
                system.stop();
                transport.sendOut("Shutdown server", SHUTDOWN);
                server.close();
                return false;
            });

            registerCommand("completer", (system, transport, args) -> {
                int completerPort = completerSock.bindToRandomPort("tcp://localhost");
                threadPool.submit(new CompletionServer(completerSock, commandNames));
                transport.sendOut(Integer.toString(completerPort));
                return true;
            });

            LOG.info("Listen " + port);
            replPort.complete(port);

            while(!Thread.currentThread().isInterrupted()) {
                ZMsg msg = ZMsg.recvMsg(server);
                ZFrame clientAddress = msg.pop();
                ZmqServerTransport transport = new ZmqServerTransport(server, clientAddress);
                String[] cmd = msg.popString().trim().split("\\s+");
                if (cmd[0].startsWith("/")) {
                    SystemCommand command = commands.get(cmd[0].substring(1));
                    if (cmd[0].isEmpty()) {
                        printHelp();
                    } else if (command != null) {
                        String[] args = new String[cmd.length - 1];
                        System.arraycopy(cmd, 1, args, 0, cmd.length - 1);
                        try {
                            boolean ret = command.execute(system, transport, args);
                            if (!ret) return;
                        } catch (Throwable ex) {
                            StringWriter traceWriter = new StringWriter();
                            ex.printStackTrace(new PrintWriter(traceWriter));
                            transport.sendErr(traceWriter.toString());
                        }
                    } else {
                        transport.sendErr("Unknown command: " + cmd[0], ReplResponse.ResponseStatus.UNKNOWN_COMMAND);
                    }
                } else {
                    transport.sendOut("");
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
        }
    }

    @Override
    public void addBackgroundTask(String name, Runnable task) {
        backgroundTasks.put(name, threadPool.submit(task));
    }

    @Override
    public Future<?> getBackground(String name) {
        Future<?> f = backgroundTasks.get(name);
        if (f == null) return null;

        if (f.isDone()) {
            backgroundTasks.remove(name);
            return null;
        }

        return f;
    }
}
