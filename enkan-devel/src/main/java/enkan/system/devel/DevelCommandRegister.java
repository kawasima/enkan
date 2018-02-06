package enkan.system.devel;

import enkan.system.Repl;
import enkan.system.devel.command.AutoResetCommand;
import enkan.system.devel.command.CompileCommand;
import enkan.system.devel.compiler.MavenCompiler;
import enkan.system.repl.SystemCommandRegister;

import java.io.Serializable;

/**
 * @author kawasima
 */
public class DevelCommandRegister implements SystemCommandRegister, Serializable {
    private Compiler compiler;

    public DevelCommandRegister() {
        this(new MavenCompiler());
    }
    /**
     * init with specified compiler.
     *
     * @param compiler Compiler
     */
    public DevelCommandRegister(final Compiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public void register(final Repl repl) {
        repl.registerCommand("autoreset", new AutoResetCommand(repl));
        repl.registerCommand("compile", new CompileCommand(compiler));
    }

    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }
}
