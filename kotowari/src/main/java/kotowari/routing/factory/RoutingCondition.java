package kotowari.routing.factory;

import enkan.collection.OptionMap;
import kotowari.routing.Route;

import java.util.regex.Pattern;

/**
 * @author kawasima
 */
public class RoutingCondition {
    private String method;
    private String path;
    private OptionMap requirements;
    private RoutePatterns.PatternsContext context;

    public RoutingCondition(String method, String path) {
        this.method = method;
        this.path = path;
        this.requirements = OptionMap.empty();
    }

    public void setContext(RoutePatterns.PatternsContext context) {
        this.context = context;
    }

    public RoutingCondition requires(String patternVariable, String pattern) {
        requirements.put(patternVariable, Pattern.compile(pattern));
        return this;
    }

    public Route to(Class<?> controllerClass, String controllerMethod) {
        OptionMap conditions = OptionMap.of("method", method);
        OptionMap options = OptionMap.of(
                "controller", controllerClass,
                "action", controllerMethod,
                "conditions", conditions);
        if (!requirements.isEmpty()) {
            options.put("requirements", requirements);
        }
        Route route = context.build(path, options);
        context.addRoute(route);
        return route;
    }
}
