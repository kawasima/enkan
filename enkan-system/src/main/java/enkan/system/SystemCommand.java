package enkan.system;

import enkan.system.repl.ReplEnvironment;

/**
 * @author kawasima
 */
public interface SystemCommand {
    boolean execute(EnkanSystem system, ReplEnvironment env, String... args);
}
