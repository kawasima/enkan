package kotowari.routing.factory;

import enkan.collection.OptionMap;
import kotowari.routing.Route;

/**
 * @author kawasima
 */
public class RoutingCondition {
    private String method;
    private String path;
    private RoutePatterns.PatternsContext context;

    public RoutingCondition(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public void setContext(RoutePatterns.PatternsContext context) {
        this.context = context;
    }

    public Route to(Class<?> controllerClass, String controllerMethod) {
        OptionMap conditions = OptionMap.of("method", method);
        OptionMap options = OptionMap.of(
                "controller", controllerClass,
                "action", controllerMethod,
                "conditions", conditions);
        Route route = context.build(path, options);
        context.addRoute(route);
        return route;
    }
}
