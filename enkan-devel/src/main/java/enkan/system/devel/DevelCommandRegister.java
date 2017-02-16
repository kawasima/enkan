package enkan.system.devel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import enkan.system.devel.compiler.MavenCompiler;

import enkan.component.ApplicationComponent;
import enkan.config.ConfigurationLoader;
import enkan.exception.FalteringEnvironmentException;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.ReplResponse;
import enkan.system.repl.SystemCommandRegister;

/**
 * @author kawasima
 */
public class DevelCommandRegister implements SystemCommandRegister {

    /** compiling build tool. */
    private Compiler compiler;

    /**
     * init with MavenCompiler.
     */
    public DevelCommandRegister() {
        this(new MavenCompiler());
    }

    /**
     * init with specified compiler.
     * @param compiler
     */
    public DevelCommandRegister(final Compiler compiler) {
        this.compiler = compiler;
    }

    protected ConfigurationLoader findConfigurationLoader(final EnkanSystem system) {
        final Optional<ConfigurationLoader> loader = system.getAllComponents().stream()
                .filter(c -> c instanceof ApplicationComponent)
                .map(c -> ((ApplicationComponent) c).getLoader())
                .filter(Objects::nonNull)
                .findFirst();
        return loader.orElseGet(null);
    }

    @Override
    public void register(final Repl repl) {
        repl.registerCommand("autoreset", (system, transport, args) -> {
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
        });

        repl.registerCommand("compile", (system, transport, args) -> {
            final CompileResult result = compiler.execute(transport);
            Throwable exception = result.getExecutionException();
            if (exception == null) {
                transport.sendOut("Finished compiling.");
            } else {
                final StringWriter sw = new StringWriter();
                //noinspection ThrowableResultOfMethodCallIgnored
                exception.printStackTrace(new PrintWriter(sw));
                sw.append("Failed to compile.");
                transport.sendErr(sw.toString());
            }
            return true;
        });
    }

    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }
}
