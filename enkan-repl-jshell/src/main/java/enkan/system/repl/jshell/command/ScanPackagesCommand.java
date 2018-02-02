package enkan.system.repl.jshell.command;

import enkan.system.EnkanSystem;
import enkan.system.ReplResponse;
import enkan.system.SystemCommand;
import enkan.system.Transport;

public class ScanPackagesCommand implements SystemCommand {
    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        system.getAllComponents().stream().forEach(
                c -> transport.send(ReplResponse.withOut(c.getClass().getName()))
        );
        return true;
    }
}
