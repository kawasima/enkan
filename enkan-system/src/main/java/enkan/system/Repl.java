package enkan.system;

import java.io.PrintStream;
import java.util.concurrent.Future;

/**
 * @author kawasima
 */
public interface Repl extends Runnable {
    PrintStream out();

    void registerCommand(String name, SystemCommand command);
    void addBackgroundTask(String name, Runnable task);

    Future<?> getBackgorundTask(String name);
}
