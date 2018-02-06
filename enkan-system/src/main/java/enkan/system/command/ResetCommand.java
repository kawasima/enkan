package enkan.system.command;

import enkan.system.EnkanSystem;
import enkan.system.SystemCommand;
import enkan.system.Transport;

public class ResetCommand implements SystemCommand {
    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        system.stop();
        system.start();
        transport.sendOut("Reset server");
        return true;
    }
}
