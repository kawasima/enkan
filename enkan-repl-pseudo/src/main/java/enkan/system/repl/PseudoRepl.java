package enkan.system.repl;

import enkan.component.WebServerComponent;
import enkan.config.EnkanSystemFactory;
import enkan.system.*;
import enkan.system.command.MiddlewareCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

import static enkan.system.ReplResponse.ResponseStatus.*;

/**
 * @author kawasima
 */
public class PseudoRepl implements Repl {
    private final Logger LOG = LoggerFactory.getLogger(PseudoRepl.class);

    private final EnkanSystem system;
    private final ExecutorService threadPool;
    private final Map<String, SystemCommand> commands = new HashMap<>();
    private final Map<String, Future<?>> backgroundTasks = new HashMap<>();
    private final CompletableFuture<Integer> replPort = new CompletableFuture<>();

    public PseudoRepl(String enkanSystemFactoryClassName) {
        try {
            system = ((Class<? extends EnkanSystemFactory>) Class.forName(enkanSystemFactoryClassName)).newInstance().create();
            threadPool = Executors.newCachedThreadPool(runnable -> {
                Thread t = new Thread(runnable);
                t.setName("enkan-repl-pseudo");
                return t;
            });
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        registerCommand("start", (system, transport, args) -> {
            system.start();
            transport.sendOut("Started server");
            if (args.length > 0) {
                if (Desktop.isDesktopSupported()) {
                    Optional<WebServerComponent> webServerComponent = system.getAllComponents().stream()
                            .filter(WebServerComponent.class::isInstance)
                            .map(WebServerComponent.class::cast)
                            .findFirst();
                    webServerComponent.ifPresent(web -> {
                        try {
                            Desktop.getDesktop().browse(URI.create("http://localhost:" + web.getPort() + "/" + args[0].replaceAll("^/", "")));
                        } catch (IOException ignore) {
                            // ignore
                        }
                    });
                }
            }
            return true;
        } );
        registerCommand("stop",  (system, transport, args) -> {
            system.stop();
            transport.sendOut("Stopped server");
            return true;
        });

        registerCommand("reset", (system, transport, args) -> {
            system.stop();
            system.start();
            transport.sendOut("Reset server");
            return true;
        });
        registerCommand("?", (system, transport, args) -> {
            commands.keySet().forEach(
                    command -> transport.send(ReplResponse.withOut("/" + command))
            );
            transport.send(new ReplResponse().done());
            return true;
        });
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
    public CompletableFuture<Integer> getPort() {
        return replPort;
    }
    @Override
    public void registerCommand(String name, SystemCommand command) {
        commands.put(name, command);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("pseudo-repl-server");
        ZContext ctx = new ZContext();
        try {
            ZMQ.Socket server = ctx.createSocket(ZMQ.ROUTER);
            int port = server.bindToRandomPort("tcp://localhost");

            registerCommand("shutdown", (system, transport, args) -> {
                system.stop();
                transport.sendOut("Shutdown server", SHUTDOWN);
                server.close();
                return false;
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
    public Future<?> getBackgorundTask(String name) {
        Future<?> f = backgroundTasks.get(name);
        if (f == null) return null;

        if (f.isDone()) {
            backgroundTasks.remove(name);
            return null;
        }

        return f;
    }
}
