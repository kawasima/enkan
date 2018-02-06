package enkan.system.command;

import enkan.system.Repl;
import enkan.system.repl.SystemCommandRegister;

/**
 * @author kawasima
 */
public class MetricsCommandRegister implements SystemCommandRegister {
    @Override
    public void register(Repl repl) {
        repl.registerCommand("metrics", new MetricsCommand());
    }
}
