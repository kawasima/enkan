package enkan.system.command;

import enkan.system.EnkanSystem;
import enkan.system.SystemCommand;
import enkan.system.Transport;

import static enkan.system.ReplResponse.ResponseStatus.SHUTDOWN;

public class ShutdownCommand implements SystemCommand {
    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        system.stop();
        transport.sendOut("Shutdown server", SHUTDOWN);
        return false;
    }
}
