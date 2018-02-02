package enkan.system.devel.command;

import enkan.component.ApplicationComponent;
import enkan.config.ConfigurationLoader;
import enkan.exception.FalteringEnvironmentException;
import enkan.system.*;
import enkan.system.devel.ClassWatcher;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AutoResetCommand implements SystemCommand {
    private Repl repl;

    public AutoResetCommand(Repl repl) {
        this.repl = repl;
    }

    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        if (repl.getBackgorundTask("classWatcher") != null) {
            transport.sendOut("Autoreset is already running.");
            return true;
        }

        final ConfigurationLoader loader = findConfigurationLoader(system);
        if (loader == null) {
            transport.sendOut("Start an application first.");
            return true;
        }
        try {
            final ClassWatcher classWatcher = new ClassWatcher(
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
        } catch (final IOException ex) {
            throw new FalteringEnvironmentException(ex);
        }
    }

    protected ConfigurationLoader findConfigurationLoader(final EnkanSystem system) {
        final Optional<ConfigurationLoader> loader = system.getAllComponents().stream()
                .filter(c -> c instanceof ApplicationComponent)
                .map(c -> ((ApplicationComponent) c).getLoader())
                .filter(Objects::nonNull)
                .findFirst();
        return loader.orElseGet(null);
    }


}
