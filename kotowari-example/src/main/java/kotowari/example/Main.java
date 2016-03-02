package kotowari.example;

import enkan.system.command.MetricsCommandRegister;
import enkan.system.devel.DevelCommandRegister;
import enkan.system.repl.PseudoRepl;
import enkan.system.repl.ReplBoot;
import kotowari.system.KotowariCommandRegister;

/**
 * @author kawasima
 */
public class Main {
    public static void main(String[] args) {
        PseudoRepl repl = new PseudoRepl(MyExampleSystemFactory.class.getName());
        ReplBoot.start(repl,
                new KotowariCommandRegister(),
                new DevelCommandRegister(),
                new MetricsCommandRegister());
    }
}
