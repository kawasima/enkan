package enkan.system;

/**
 * @author kawasima
 */
public interface SystemCommand {
    boolean execute(EnkanSystem system, String... args);
}
