package enkan.system.command;

import enkan.system.EnkanSystem;
import enkan.system.ReplResponse;
import enkan.system.SystemCommand;
import enkan.system.Transport;

import java.util.Set;

/**
 * The HelpCommand class provides a method to execute a help command.
 * It sends a response with a list of available commands.
 *
 * @author kawasima
 */
public class HelpCommand implements SystemCommand {
    private final Set<String> commands;

    public HelpCommand(Set<String> commands) {
        this.commands = commands;
    }

    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        commands.forEach(
                command -> transport.send(ReplResponse.withOut("/" + command))
        );
        transport.send(new ReplResponse().done());
        return true;
    }
}
