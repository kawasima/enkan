package enkan.system.repl;

import enkan.component.WebServerComponent;
import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.SystemCommand;
import enkan.system.command.MiddlewareCommand;
import enkan.system.repl.pseudo.Transport;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author kawasima
 */
public class PseudoRepl implements Repl {
    private EnkanSystem system;
    private ExecutorService threadPool;
    private Map<String, SystemCommand> commands = new HashMap<>();
    private Map<String, Future<?>> backgroundTasks = new HashMap<>();

    public PseudoRepl(String enkanSystemFactoryClassName) {
        try {
            system = ((Class<? extends EnkanSystemFactory>) Class.forName(enkanSystemFactoryClassName)).newInstance().create();
            threadPool = Executors.newCachedThreadPool();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        registerCommand("start", (system, env, args) -> {
            system.start();
            env.out.println("Started server");
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
        registerCommand("stop",  (system, env, args) -> {
            system.stop();
            env.out.println("Stopped server");
            return true;
        });

        registerCommand("reset", (system, env, args) -> {
            system.stop();
            system.start();
            env.out.println("Reset server");
            return true;
        });
        registerCommand("shutdown",  (system, env, args) -> { system.stop(); return false; });
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
        try {
            InetSocketAddress addr = new InetSocketAddress("localhost", 0);
            ServerSocket serverSock = new ServerSocket();
            serverSock.setReuseAddress(true);
            serverSock.bind(addr);

            System.out.println("Listen " + serverSock.getLocalPort());

            do {
                Socket socket = serverSock.accept();
                PrintStream ps = new PrintStream(socket.getOutputStream(), true);
                ReplEnvironment env = new ReplEnvironment(ps, ps);

                threadPool.submit(new Transport(socket, msg -> {
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
                            command.execute(system, env, args);
                        } else {
                            ps.println("Unknown command: " + cmd[0]);
                        }
                    }
                }));
            } while(!serverSock.isClosed());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
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
