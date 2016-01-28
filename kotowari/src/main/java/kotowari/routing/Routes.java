package kotowari.routing;

import enkan.collection.OptionMap;
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
