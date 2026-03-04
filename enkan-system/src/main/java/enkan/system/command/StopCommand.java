package enkan.system.command;

import enkan.system.EnkanSystem;
import enkan.system.SystemCommand;
import enkan.system.Transport;

public class StopCommand implements SystemCommand {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        system.stop();
        transport.sendOut("Stopped server");
        return true;
    }
}
