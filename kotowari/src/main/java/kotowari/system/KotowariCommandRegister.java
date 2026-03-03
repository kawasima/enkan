package kotowari.system;

import enkan.Application;
import enkan.MiddlewareChain;
import enkan.component.ApplicationComponent;
import enkan.component.SystemComponent;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.system.Repl;
import enkan.system.ReplResponse;
import enkan.system.repl.SystemCommandRegister;
import kotowari.middleware.RoutingMiddleware;

/**
 * Registers commands for routes.
 *
 * @author kawasima
 */
public class KotowariCommandRegister implements SystemCommandRegister {
    public void register(Repl repl) {
        repl.registerCommand("routes", (system, transport, args) -> {
            if (args == null || args.length == 0) {
                transport.sendOut("usage: routes [app]");
                return true;
            }

            String appName = args[0];
            SystemComponent<?> component = system.getComponent(appName);
            if (component instanceof ApplicationComponent) {
                Application<HttpRequest, HttpResponse> app = ((ApplicationComponent<HttpRequest, HttpResponse>) component).getApplication();
                if (app == null) {
                    transport.sendErr(String.format("Application %s is not running.", appName));
                    return true;
                }
                app.getMiddlewareStack().stream()
                        .map(MiddlewareChain::getMiddleware)
                        .filter(middleware -> middleware instanceof RoutingMiddleware)
                        .forEach(m -> transport.send(ReplResponse.withOut(((RoutingMiddleware) m).getRoutes().toString())));
                transport.sendOut("", ReplResponse.ResponseStatus.DONE);
            } else {
                transport.sendErr(String.format("Application %s not found.", appName));
            }
            return true;
        });
    }
}
