package enkan.system.repl.command;

import enkan.system.EnkanSystem;
import enkan.system.SystemCommand;
import enkan.system.Transport;

public class CompleterCommand implements SystemCommand {
    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        return false;
    }
}
