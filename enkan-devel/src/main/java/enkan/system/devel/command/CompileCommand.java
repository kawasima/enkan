package enkan.system.devel.command;

import enkan.system.EnkanSystem;
import enkan.system.SystemCommand;
import enkan.system.Transport;
import enkan.system.devel.CompileResult;
import enkan.system.devel.Compiler;
import enkan.system.devel.compiler.MavenCompiler;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CompileCommand implements SystemCommand {
    /** compiling build tool. */
    private transient Compiler compiler;

    /**
     * init with MavenCompiler.
     */
    public CompileCommand() {
        this(new MavenCompiler());
    }

    public CompileCommand(Compiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
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
    }
}
