package enkan.system;

import java.util.concurrent.Future;

/**
 * The interface for a REPL server.
 *
 * @author kawasima
 */
public interface Repl extends Runnable {
    /**
     * Register a command into REPL.
     *
     * @param name    The name of the given command
     * @param command A command object
     */
    void registerCommand(String name, SystemCommand command);

    /**
     * Add a background task to REPL.
     *
     * @param name The name of the given background task
     * @param task
     */
    void addBackgroundTask(String name, Runnable task);

    /**
     * Get a future object for a background task.
     *
     * @param name The name of a background task
     * @return The future of background task
     */
    Future<?> getBackgorundTask(String name);

    /**
     * Get the port number of this REPL.
     *
     * @return the port number
     */
    Integer getPort();
}
