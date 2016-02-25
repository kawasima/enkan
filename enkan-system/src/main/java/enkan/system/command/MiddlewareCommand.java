package enkan.system.command;

import enkan.Application;
import enkan.MiddlewareChain;
import enkan.component.ApplicationComponent;
import enkan.component.SystemComponent;
import enkan.predicate.AnyPredicate;
import enkan.predicate.NonePredicate;
import enkan.system.EnkanSystem;
import enkan.system.ReplResponse;
import enkan.system.SystemCommand;
import enkan.system.Transport;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * @author kawasima
 */
public class MiddlewareCommand implements SystemCommand {
    private void list(Application<?, ?> app, Transport transport) {
        List<MiddlewareChain<?, ?>> chains = app.getMiddlewareStack();
        chains.forEach(chain -> transport.send(ReplResponse.withOut(chain.toString())));
    }

    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        if (args == null || args.length < 2) {
            transport.sendOut("middleware [appName]");
            return true;
        }

        String appName = args[0];
        SystemComponent component = system.getComponent(appName);
        if (component == null || !(component instanceof ApplicationComponent)) {
            transport.sendErr(String.format("Application %s not found.", appName));
            return true;
        }
        Application<?, ?> app = ((ApplicationComponent) component).getApplication();

        switch (args[1]) {
            case "list":
                list(app, transport);
                transport.sendOut("", ReplResponse.ResponseStatus.DONE);
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
                        transport.sendOut(String.format(Locale.US, "Middleware %s's predicate has changed to %s.", middlewareName, predicateName));
                    } else {
                        transport.sendOut(String.format(Locale.US, "Usage: /middleware [app name] predicate [middleware name] [predicate nme]"));
                    }
                } else {
                    transport.sendErr(String.format("Middleware %s not found.", middlewareName));
                }
                break;
            default:
                transport.sendErr("");
        }

        return true;
    }
}
