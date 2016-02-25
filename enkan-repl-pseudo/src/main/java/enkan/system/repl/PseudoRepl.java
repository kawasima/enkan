package enkan.system.repl;

import enkan.component.WebServerComponent;
import enkan.config.EnkanSystemFactory;
import enkan.exception.FalteringEnvironmentException;
import enkan.system.*;
import enkan.system.command.MiddlewareCommand;
import enkan.system.repl.pseudo.PseudoReplTransport;
import enkan.system.repl.pseudo.ReplClient;
import org.msgpack.MessagePack;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static enkan.system.ReplResponse.ResponseStatus.SHUTDOWN;

/**
 * @author kawasima
 */
public class PseudoRepl implements Repl {
    private final Logger LOG = LoggerFactory.getLogger(PseudoRepl.class);

    private EnkanSystem system;
    private ExecutorService threadPool;
    private Map<String, SystemCommand> commands = new HashMap<>();
    private Map<String, Future<?>> backgroundTasks = new HashMap<>();

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
    public void registerCommand(String name, SystemCommand command) {
        commands.put(name, command);
    }

    @Override
    public void run() {
        ReplClient replClient = null;
        try (ServerSocket serverSock = new ServerSocket()) {
            InetSocketAddress addr = new InetSocketAddress("localhost", 0);
            serverSock.setReuseAddress(true);
            serverSock.bind(addr);

            registerCommand("shutdown", (system, transport, args) -> {
                system.stop();
                transport.sendOut("Shutdown server", SHUTDOWN);
                try {
                    serverSock.close();
                    return false;
                } catch (IOException ex) {
                    throw FalteringEnvironmentException.create(ex);
                }
            });

            System.out.println("Listen " + serverSock.getLocalPort());

            replClient = new ReplClient();
            replClient.start(serverSock.getLocalPort());

            do {
                Socket socket = serverSock.accept();
                Transport transport = new PseudoReplTransport(socket);

                threadPool.submit(() -> {
                    try {
                        Unpacker unpacker = new MessagePack().createUnpacker(socket.getInputStream());

                        while (true) {
                            String msg = unpacker.readString();
                            String[] cmd = msg.trim().split("\\s+");
                            if (cmd[0].startsWith("/")) {
                                SystemCommand command = commands.get(cmd[0].substring(1));
                                if (cmd[0].isEmpty()) {
                                    printHelp();
                                } else if (command != null) {
                                    String[] args = new String[cmd.length - 1];
                                    if (cmd.length > 0) {
                                        System.arraycopy(cmd, 1, args, 0, cmd.length - 1);
                                    }
                                    boolean ret = command.execute(system, transport, args);
                                    if (!ret) return;
                                } else {
                                    transport.sendErr("Unknown command: " + cmd[0], ReplResponse.ResponseStatus.UNKNOWN_COMMAND);
                                }
                            } else {
                                transport.sendOut("");
                            }
                        }
                    } catch (EOFException ignore) {
                        // Disconnect from client.
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException ignore) {

                        }
                    }
                });
            } while (!serverSock.isClosed());
        } catch (SocketException e) {
            LOG.info("Repl server closed");
        } catch (Exception e) {
            LOG.error("Repl server error", e);
        } finally {
            try {
                threadPool.shutdown();
                if (!threadPool.awaitTermination(3L, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
            }
            LOG.info("REPL client close");
            replClient.close();
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
