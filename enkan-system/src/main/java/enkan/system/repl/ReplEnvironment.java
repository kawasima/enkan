package enkan.system.repl;

import java.io.PrintStream;

/**
 * @author kawasima
 */
public class ReplEnvironment {
    public PrintStream out;
    public PrintStream err;

    public ReplEnvironment(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

}
