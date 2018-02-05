package kotowari.example;

import enkan.system.Repl;
import enkan.system.command.JsonRequestCommand;
import enkan.system.command.MetricsCommandRegister;
import enkan.system.command.SqlCommand;
import enkan.system.repl.JShellRepl;
import enkan.system.repl.PseudoRepl;
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
        //JShellRepl repl = new JShellRepl(ExampleSystemFactory.class.getName());
        PseudoRepl repl = new PseudoRepl(ExampleSystemFactory.class.getName());

        ReplBoot.start(repl,
                new KotowariCommandRegister(),
                new MetricsCommandRegister(),
                r -> {
                    r.registerCommand("sql", new SqlCommand());
                    r.registerCommand("jsonRequest", new JsonRequestCommand());
                });

        new ReplClient().start(repl.getPort());
    }
}
