package kotowari.example;

import enkan.system.command.JsonRequestCommand;
import enkan.system.command.MetricsCommandRegister;
import enkan.system.command.SqlCommand;
import enkan.system.devel.command.CompileCommand;
import enkan.system.repl.JShellRepl;
import enkan.system.repl.ReplBoot;
import enkan.system.repl.client.ReplClient;
import kotowari.system.KotowariCommandRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A main class for development.
 *
 * @author kawasima
 */
public class DevMain {
    private static final Logger LOG = LoggerFactory.getLogger(DevMain.class);
    public static void main(String[] args) {
        JShellRepl repl = new JShellRepl(ExampleSystemFactory.class.getName());

        ReplBoot.start(repl,
                new KotowariCommandRegister(),
                new MetricsCommandRegister(),
                r -> {
                    r.registerCommand("sql", new SqlCommand());
                    r.registerCommand("jsonRequest", new JsonRequestCommand());
                    r.registerCommand("compile", new CompileCommand());
                });

        LOG.info("Dev mode started. REPL is listening on port {}", repl.getPort());
        new ReplClient().start(repl.getPort());
    }
}
