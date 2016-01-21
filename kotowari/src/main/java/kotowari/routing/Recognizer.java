package kotowari.routing;

import enkan.collection.OptionMap;

import java.util.List;

/**
 * @author kawasima
 */
public interface Recognizer {
    void setRoutes(List<Route> routes);
    OptionMap recognize(String path, String method);
    boolean isOptimized();
    void optimize();
}
