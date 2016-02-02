package kotowari.system;

import enkan.Application;
import enkan.component.ApplicationComponent;
import enkan.component.SystemComponent;
import enkan.system.repl.PseudoRepl;
import enkan.system.repl.SystemCommandRegister;
import kotowari.middleware.RoutingMiddleware;

/**
 * @author kawasima
 */
public class KotowariCommandRegister implements SystemCommandRegister {
    public void register(PseudoRepl repl) {
        repl.registerCommand("routes", (system, args) -> {
            if (args == null || args.length == 0) {
                repl.out().println("usage: routes [app]");
                return true;
            }

            String appName = args[0];
            SystemComponent component = system.getComponent(appName);
            if (component instanceof ApplicationComponent) {
                Application<?, ?> app = ((ApplicationComponent) component).getApplication();
                if (app == null) {
                    repl.out().println(String.format("Application %s is not running.", appName));
                    return true;
                }
                app.getMiddlewareStack().stream()
                        .map(chain -> chain.getMiddleware())
                        .filter(middleware -> middleware instanceof RoutingMiddleware)
                        .map(m -> ((RoutingMiddleware) m).getRoutes().toString())
                        .forEach(System.out::println);
            } else {
                repl.out().println(String.format("Application %s not found.", appName));
            }
            return true;
        });
    }
}
