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

import java.util.List;
import java.util.Optional;

/**
 * @author kawasima
 */
public class MiddlewareCommand implements SystemCommand {
    private Repl repl;

    public MiddlewareCommand(Repl repl) {
        this.repl = repl;
    }

    private void list(Application<?, ?> app) {
        List<MiddlewareChain<?, ?>> chains = app.getMiddlewareStack();
        chains.forEach(chain -> repl.out().println(chain.toString()));
    }

    @Override
    public boolean execute(EnkanSystem system, String... args) {
        if (args == null || args.length < 2) {
            repl.out().println("middleware [appName]");
            return true;
        }

        String appName = args[0];
        SystemComponent component = system.getComponent(appName);
        if (component == null || !(component instanceof ApplicationComponent)) {
            repl.out().println(String.format("Application %s not found.", appName));
            return true;
        }
        Application<?, ?> app = ((ApplicationComponent) component).getApplication();

        switch (args[1]) {
            case "list":
                list(app);
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
                    repl.out().println(String.format("Middleware %s not found.", middlewareName));
                }
        }

        return true;
    }
}
