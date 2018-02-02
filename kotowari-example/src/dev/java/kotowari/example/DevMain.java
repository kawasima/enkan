package kotowari.example;

import enkan.system.command.MetricsCommandRegister;
import enkan.system.devel.DevelCommandRegister;
import enkan.system.repl.JShellRepl;
import enkan.system.repl.ReplBoot;
import enkan.system.repl.client.ReplClient;
import kotowari.system.KotowariCommandRegister;

/**
 * A main class for development.
 *
 * @author kawasima
 */
public class DevMain {
    public static void main(String[] args) throws Exception {
        JShellRepl repl = new JShellRepl(ExampleSystemFactory.class.getName());

        ReplBoot.start(repl,
                new KotowariCommandRegister());

        new ReplClient().start(repl.getPort());
    }
}
