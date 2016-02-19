package enkan.system;

import java.util.concurrent.Future;

/**
 * @author kawasima
 */
public interface Repl extends Runnable {
    void registerCommand(String name, SystemCommand command);
    void addBackgroundTask(String name, Runnable task);

    Future<?> getBackgorundTask(String name);
}
