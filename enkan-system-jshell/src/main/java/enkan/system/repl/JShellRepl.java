package enkan.system.repl;

import enkan.system.Repl;
import enkan.system.SystemCommand;

import java.io.PrintStream;
import java.util.concurrent.Future;

/**
 * @author kawasima
 */
public class JShellRepl implements Repl {
    @Override
    public PrintStream out() {
        return null;
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
        //JShell.builder();
    }
}
