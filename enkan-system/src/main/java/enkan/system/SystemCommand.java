package enkan.system;

import java.io.Serializable;

/**
 * A command for Enkan system.
 *
 * @author kawasima
 */
public interface SystemCommand extends Serializable {
    /**
     * Execute this command.
     *
     * @param system Enkan system
     * @param transport A transport
     * @param args arguments
     * @return true if the command will terminate the REPL, otherwise false.
     */
    boolean execute(EnkanSystem system, Transport transport, String... args);
}
