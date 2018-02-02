package kotowari.scaffold.command;

import enkan.system.Repl;
import enkan.system.SystemCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author kawasima
 */
public class ReplMock implements Repl {
    private Map<String, SystemCommand> commands = new HashMap<>();

    @Override
    public void registerCommand(String name, SystemCommand command) {
        commands.put(name, command);
    }

    public SystemCommand getCommand(String name) {
        return commands.get(name);
    }

    @Override
    public void addBackgroundTask(String name, Runnable task) {

    }

    @Override
    public Integer getPort() {
        return null;
    }

    @Override
    public Future<?> getBackgorundTask(String name) {
        return null;
    }

    @Override
    public void run() {

    }
}
