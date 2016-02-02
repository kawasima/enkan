package enkan.system;

import java.io.PrintStream;

/**
 * @author kawasima
 */
public interface Repl extends Runnable {
    PrintStream out();

    void registerCommand(String name, SystemCommand command);
}
