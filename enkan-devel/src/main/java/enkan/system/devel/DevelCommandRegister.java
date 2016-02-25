package enkan.system.devel;

import enkan.component.ApplicationComponent;
import enkan.config.ConfigurationLoader;
import enkan.exception.FalteringEnvironmentException;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.ReplResponse;
import enkan.system.repl.SystemCommandRegister;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class DevelCommandRegister implements SystemCommandRegister {
    protected ConfigurationLoader findConfigurationLoader(EnkanSystem system) {
        Optional<ConfigurationLoader> loader = system.getAllComponents().stream()
                .filter(c -> c instanceof ApplicationComponent)
                .map(c -> ((ApplicationComponent) c).getLoader())
                .filter(l -> l != null)
                .findFirst();
        return loader.orElseGet(null);
    }

    @Override
    public void register(Repl repl) {
        repl.registerCommand("autoreset", (system, transport, args) -> {
            if (repl.getBackgorundTask("classWatcher") != null) {
                transport.sendOut("Autoreset is already running.");
                return true;
            }

            ConfigurationLoader loader = findConfigurationLoader(system);
            if (loader == null) {
                transport.sendOut("Start an application first.");
                return true;
            }
            try {
                ClassWatcher classWatcher = new ClassWatcher(
                        loader.reloadableFiles().stream()
                                .map(File::toPath)
                        .collect(Collectors.toSet()),
                        () -> {
                            system.stop();
                            system.start();
                            transport.send(ReplResponse.withOut("Reset automatically"));
                    });
                repl.addBackgroundTask("classWatcher", classWatcher);
                transport.sendOut("Start to watch modification an application.");
                return true;
            } catch (IOException ex) {
                throw FalteringEnvironmentException.create(ex);
            }
        });
    }
}
