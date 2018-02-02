package enkan.system;

import java.io.Serializable;

/**
 * A command for Enkan system.
 *
 * @author kawasima
 */
public interface SystemCommand extends Serializable {
    boolean execute(EnkanSystem system, Transport transport, String... args);
}
