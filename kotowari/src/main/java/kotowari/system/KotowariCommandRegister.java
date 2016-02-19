package kotowari.system;

import enkan.Application;
import enkan.component.ApplicationComponent;
import enkan.component.SystemComponent;
import enkan.system.Repl;
import enkan.system.repl.SystemCommandRegister;
import kotowari.middleware.RoutingMiddleware;

/**
 * @author kawasima
 */
public class KotowariCommandRegister implements SystemCommandRegister {
    public void register(Repl repl) {
        repl.registerCommand("routes", (system, env, args) -> {
            if (args == null || args.length == 0) {
                env.out.println("usage: routes [app]");
                return true;
            }

            String appName = args[0];
            SystemComponent component = system.getComponent(appName);
            if (component instanceof ApplicationComponent) {
                Application<?, ?> app = ((ApplicationComponent) component).getApplication();
                if (app == null) {
                    env.out.println(String.format("Application %s is not running.", appName));
                    return true;
                }
                app.getMiddlewareStack().stream()
                        .map(chain -> chain.getMiddleware())
                        .filter(middleware -> middleware instanceof RoutingMiddleware)
                        .map(m -> ((RoutingMiddleware) m).getRoutes().toString())
                        .forEach(env.out::println);
            } else {
                env.out.println(String.format("Application %s not found.", appName));
            }
            return true;
        });
    }
}
