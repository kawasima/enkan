package enkan.system.devel.command;

import enkan.exception.FalteringEnvironmentException;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.SystemCommand;
import enkan.system.Transport;
import enkan.system.devel.ClassWatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class AutoResetCommand implements SystemCommand {
    private final transient Repl repl;

    public AutoResetCommand(Repl repl) {
        this.repl = repl;
    }

    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        if (repl.getBackground("classWatcher") != null) {
            transport.sendOut("Autoreset is already running.");
            return true;
        }

        try {
            final ClassWatcher classWatcher = new ClassWatcher(
                    watchPaths(),
                    () -> repl.eval("system.stop(); system.start()", transport));
            repl.addBackgroundTask("classWatcher", classWatcher);
            transport.sendOut("Start to watch modification of an application.");
            return true;
        } catch (IOException ex) {
            throw new FalteringEnvironmentException(ex);
        }
    }

    private Set<Path> watchPaths() {
        return Set.of(new File("target/classes").toPath());
    }
}
