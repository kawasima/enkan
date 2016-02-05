package enkan.system.repl;

import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.SystemCommand;
import enkan.system.command.MiddlewareCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
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

        registerCommand("start", (system, args) -> { system.start(); return true; } );
        registerCommand("stop",  (system, args) -> { system.stop(); return true; });
        registerCommand("reset", (system, args) -> { system.stop(); system.start(); return true; });
        registerCommand("exit",  (system, args) -> { system.stop(); return false; });
        registerCommand("middleware", new MiddlewareCommand(this));
    }

    protected void printHelp() {
        System.out.println("start - Start system\n" +
                "stop - Stop system.\n" +
                "reset - Reset system.\n" +
                "exit - exit repl.\n"
        );
    }

    protected boolean repl(BufferedReader reader) throws IOException, URISyntaxException {
        System.out.print("REPL> ");
        String[] cmd = reader.readLine().trim().split("\\s+");
        SystemCommand command = commands.get(cmd[0]);
        if (cmd[0].isEmpty()) {
            printHelp();
        } else if (command != null) {
            String[] args = new String[cmd.length - 1];
            if (cmd.length > 0) {
                System.arraycopy(cmd, 1, args, 0, cmd.length - 1);
            }
            return command.execute(system, args);
        } else {
            System.out.println("Unknown command: " + cmd[0]);
        }
        return true;
    }

    @Override
    public PrintStream out() {
        return System.out;
    }

    @Override
    public void registerCommand(String name, SystemCommand command) {
        commands.put(name, command);
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            for(;;) {
                if (!repl(reader)) break;
            }
            threadPool.shutdown();
        } catch (Throwable ex) {
            ex.printStackTrace();
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
