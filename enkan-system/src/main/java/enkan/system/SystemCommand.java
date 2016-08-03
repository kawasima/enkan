package enkan.system;

/**
 * A command for Enkan system.
 *
 * @author kawasima
 */
public interface SystemCommand {
    boolean execute(EnkanSystem system, Transport transport, String... args);
}
