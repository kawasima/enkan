package enkan.system.command;

import com.codahale.metrics.*;
import enkan.component.metrics.MetricsComponent;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.ReplResponse;
import enkan.system.Transport;
import enkan.system.repl.SystemCommandRegister;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author kawasima
 */
public class MetricsCommandRegister implements SystemCommandRegister {
    @Override
    public void register(Repl repl) {
        repl.registerCommand("metrics", new MetricsCommand());
    }
}
