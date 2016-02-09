package kotowari.routing;

import enkan.collection.OptionMap;
import enkan.exception.MisconfigurationException;
import kotowari.routing.factory.RoutePatterns;
import kotowari.routing.factory.RoutePatternsDescriptor;
import kotowari.routing.recognizer.OptimizedRecognizer;

import java.util.List;

/**
 * @author kawasima
 */
public class Routes {
    private Recognizer recognizer;
    private List<Route> routeList;

    private Routes() {
        recognizer = new OptimizedRecognizer();
    }

    public static RoutePatterns define(RoutePatternsDescriptor descriptor) {
        RoutePatterns patterns = new RoutePatterns(new Routes());
        descriptor.describe(patterns);
        return patterns;
    }

    // TODO to be private
    public void setRouteList(List<Route> routeList) {
        this.routeList = routeList;
        recognizer.setRoutes(routeList);
        recognizer.optimize();
    }

    public OptionMap recognizePath(String path, String method) {
        return recognizer.recognize(path, method);
    }

    public String generate(OptionMap options) {
        OptionMap merged = OptionMap.of(options);
        Class<?> controller = (Class<?>) options.get("controller");
        String action = options.getString("action");

        if (controller == null || action == null) {
            throw MisconfigurationException.create("ROUTING_GENERATION");
        }
        return routeList.stream()
                .filter(r -> {
                    boolean b = r.matchesControllerAndAction(controller, action);
                    return b;
                })
                .filter(r -> r.significantKeys().stream().allMatch(k -> options.containsKey(k)))
                .map(r -> r.generate(options, merged))
                .findFirst()
                .orElseThrow(() -> MisconfigurationException.create("ROUTING_GENERATION"));

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
