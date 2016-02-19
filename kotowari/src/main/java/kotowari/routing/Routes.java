package kotowari.routing;

import enkan.collection.OptionMap;
import enkan.exception.MisconfigurationException;
import kotowari.routing.factory.RoutePatterns;
import kotowari.routing.factory.RoutePatternsDescriptor;
import kotowari.routing.recognizer.OptimizedRecognizer;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> MisconfigurationException.create("ROUTING_GENERATION",
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

    public static int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

        for (int i = 0; i <= lhs.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= rhs.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= lhs.length(); i++)
            for (int j = 1; j <= rhs.length(); j++)
                distance[i][j] = Math.min(Math.min(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1),
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

        return distance[lhs.length()][rhs.length()];
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
