package enkan.system;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author kawasima
 */
public interface Repl extends Runnable {
    void registerCommand(String name, SystemCommand command);
    void addBackgroundTask(String name, Runnable task);

    CompletableFuture<Integer> getPort();
    Future<?> getBackgorundTask(String name);
}
