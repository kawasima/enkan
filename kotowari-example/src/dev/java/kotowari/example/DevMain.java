package kotowari.example;

import enkan.system.command.MetricsCommandRegister;
import enkan.system.devel.DevelCommandRegister;
import enkan.system.repl.PseudoRepl;
import enkan.system.repl.ReplBoot;
import enkan.system.repl.pseudo.ReplClient;
import kotowari.system.KotowariCommandRegister;

/**
 * A main class for development.
 *
 * @author kawasima
 */
public class DevMain {
    public static void main(String[] args) throws Exception {
        PseudoRepl repl = new PseudoRepl(ExampleSystemFactory.class.getName());
        ReplBoot.start(repl,
                new KotowariCommandRegister(),
                new DevelCommandRegister(),
                new MetricsCommandRegister());

        new ReplClient().start(repl.getPort().get());
    }
}
