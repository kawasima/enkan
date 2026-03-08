package enkan.system.command;

import enkan.system.EnkanSystem;
import enkan.system.SystemCommand;
import enkan.system.Transport;

public class StatusCommand implements SystemCommand {
    private static final long serialVersionUID = 1L;

    @Override
    public String shortDescription() {
        return "Show the system status";
    }

    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        if (system.isStarted()) {
            transport.sendOut("System is started");
        } else {
            transport.sendOut("System is stopped");
        }
        return false;
    }
}
