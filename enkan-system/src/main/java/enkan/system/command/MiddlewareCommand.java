package enkan.system.command;

import enkan.Application;
import enkan.MiddlewareChain;
import enkan.component.ApplicationComponent;
import enkan.component.SystemComponent;
import enkan.predicate.AnyPredicate;
import enkan.predicate.NonePredicate;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.SystemCommand;
import enkan.system.repl.ReplEnvironment;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

/**
 * @author kawasima
 */
public class MiddlewareCommand implements SystemCommand {
    private void list(Application<?, ?> app, PrintStream out) {
        List<MiddlewareChain<?, ?>> chains = app.getMiddlewareStack();
        chains.forEach(chain -> out.println(chain.toString()));
    }

    @Override
    public boolean execute(EnkanSystem system, ReplEnvironment env, String... args) {
        if (args == null || args.length < 2) {
            env.out.println("middleware [appName]");
            return true;
        }

        String appName = args[0];
        SystemComponent component = system.getComponent(appName);
        if (component == null || !(component instanceof ApplicationComponent)) {
            env.out.println(String.format("Application %s not found.", appName));
            return true;
        }
        Application<?, ?> app = ((ApplicationComponent) component).getApplication();

        switch (args[1]) {
            case "list":
                list(app, env.out);
                break;
            case "predicate":
                String middlewareName = args[2];

                Optional<MiddlewareChain<?,?>> middlewareChain = app.getMiddlewareStack().stream()
                        .filter(chain -> chain.getName().equals(middlewareName))
                        .findFirst();

                if (middlewareChain.isPresent()) {
                    if (args.length > 3) {
                        String predicateName = args[3];
                        switch (predicateName) {
                            case "ANY":
                                middlewareChain.get().setPredicate(new AnyPredicate<>());
                                break;
                            case "NONE":
                                middlewareChain.get().setPredicate(new NonePredicate<>());
                                break;
                        }

                    }
                } else {
                    env.out.println(String.format("Middleware %s not found.", middlewareName));
                }
        }

        return true;
    }
}
