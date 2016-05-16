package enkan.system.devel;

import enkan.component.ApplicationComponent;
import enkan.config.ConfigurationLoader;
import enkan.exception.FalteringEnvironmentException;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.ReplResponse;
import enkan.system.repl.SystemCommandRegister;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.cli.CommandLineException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
        MavenCompiler compiler = new MavenCompiler();
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
                throw new FalteringEnvironmentException(ex);
            }
        });

        repl.registerCommand("compile", (system, transport, args) -> {
            try {
                InvocationResult result = compiler.execute();
                if (result.getExitCode() == 0) {
                    transport.sendOut("Finished compiling.");
                } else {
                    StringWriter sw = new StringWriter();
                    CommandLineException exception = result.getExecutionException();
                    if (exception != null) {
                        exception.printStackTrace(new PrintWriter(sw));
                    }
                    sw.append("Failed to compile.");
                    transport.sendErr(sw.toString());
                }
            } catch (MavenInvocationException ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                transport.sendErr(sw.toString());
            }
            return true;
        });
    }
}
