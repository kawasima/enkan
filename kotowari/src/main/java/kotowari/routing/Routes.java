package kotowari.routing;

import enkan.collection.OptionMap;
import enkan.exception.MisconfigurationException;
import kotowari.routing.factory.RoutePatterns;
import kotowari.routing.factory.RoutePatternsDescriptor;
import kotowari.routing.recognizer.OptimizedRecognizer;

import java.util.List;
import java.util.stream.Collectors;

import static enkan.util.SearchUtils.levenshteinDistance;

/**
 * @author kawasima
 */
public class Routes {
    private Recognizer recognizer;
    private List<Route> routeList;

    private Routes(List<Route> routeList) {
        this.routeList = routeList;
        recognizer = new OptimizedRecognizer();
    }

    public static RoutePatterns define(RoutePatternsDescriptor descriptor) {
        return define(null, descriptor);
    }
    public static RoutePatterns define(String prefix, RoutePatternsDescriptor descriptor) {
        RoutePatterns patterns = new RoutePatterns(prefix, routeList -> {
            Routes routes = new Routes(routeList);
            routes.recognizer.setRoutes(routeList);
            routes.recognizer.optimize();
            return routes;
        });
        descriptor.describe(patterns);
        return patterns;
    }

    public OptionMap recognizePath(String path, String method) {
        return recognizer.recognize(path, method);
    }

    public String generate(OptionMap options) {
        OptionMap merged = OptionMap.of(options);
        Class<?> controller = (Class<?>) options.get("controller");
        String action = options.getString("action");

        if (controller == null || action == null) {
            throw new MisconfigurationException("kotowari.ROUTING_GENERATION");
        }
        return routeList.stream()
                .filter(r -> {
                    boolean b = r.matchesControllerAndAction(controller, action);
                    return b;
                })
                .filter(r -> r.significantKeys().stream().allMatch(options::containsKey))
                .map(r -> r.generate(options, merged))
                .findFirst()
                .orElseThrow(() -> new MisconfigurationException("kotowari.ROUTING_GENERATION",
                        controller, action,
                        routeList.stream()
                                .filter(r -> r.matchesController(controller))
                                .map(Object::toString)
                                .collect(Collectors.joining("")),
                        routeList.stream()
                                .filter(r -> r.matchesController(controller))
                                .map(Route::getActionRequirement)
                                .sorted((a, b) ->
                                        levenshteinDistance(a, action) - levenshteinDistance(b, action))
                                .findFirst()
                                .orElse("")
                ));

    }



    @Override
    public String toString() {
        if (routeList == null) {
            return "[empty]";
        }
        StringBuilder out = new StringBuilder(1024);
        for (Route route : routeList) {
            out.append(route.toString());
        }
        return out.toString();
    }
}
