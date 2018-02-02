package enkan.system.command;

import enkan.system.EnkanSystem;
import enkan.system.ReplResponse;
import enkan.system.SystemCommand;
import enkan.system.Transport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class HelpCommand implements SystemCommand {
    private Set<String> commands;

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
