package enkan.system.repl;

import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.SystemCommand;
import jdk.jshell.JShell;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author kawasima
 */
public class JShellRepl implements Repl {
    private EnkanSystem system;
    private ExecutorService threadPool;
    private Map<String, SystemCommand> commands = new HashMap<>();
    private Map<String, Future<?>> backgroundTasks = new HashMap<>();

    public JShellRepl(String enkanSystemFactoryClassName) {
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
    }
    
    @Override
    public void registerCommand(String name, SystemCommand command) {

    }

    @Override
    public void addBackgroundTask(String name, Runnable task) {

    }

    @Override
    public Future<?> getBackgorundTask(String name) {
        return null;
    }

    @Override
    public void run() {
        JShell jshell = JShell.builder()
                .build();
    }
}
