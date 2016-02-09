package kotowari.routing.factory;

import enkan.collection.OptionMap;
import kotowari.routing.Route;
import kotowari.routing.RouteBuilder;
import kotowari.routing.Routes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Routing patterns.
 *
 * @author kawasima
 */
public class RoutePatterns {
    private List<Route> routeList;
    private Routes routes;
    private PatternsContext context;
    private RouteBuilder builder;

    public RoutePatterns(Routes routes) {
        this.routes = routes;
        routeList = new ArrayList<>();
        builder = new RouteBuilder();
        context = new PatternsContext(this);
    }

    RoutingCondition httpMethodCondition(String method, String path) {
        RoutingCondition cond = new RoutingCondition(method, path);
        cond.setContext(context);
        return cond;
    }

    public RoutingCondition get(String path) {
        return httpMethodCondition("GET", path);
    }

    public RoutingCondition post(String path) {
        return httpMethodCondition("POST", path);
    }

    public RoutingCondition put(String path) {
        return httpMethodCondition("PUT", path);
    }

    public RoutingCondition delete(String path) {
        return httpMethodCondition("DELETE", path);
    }

    public Route resource(Class<?> controller) {
        return resource(controller, null);
    }

    private String decapitalize(String s) {
        if (s != null && s.length() > 1) {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        } else {
            return s;
        }
    }
    public Route resource(Class<?> controller, OptionMap options) {
        String name = decapitalize(controller.getSimpleName().replaceAll("Controller$", ""));
        get(name + "/").to(controller, "index");
        get(name + "/:id"     ).requires("id", "\\d+").to(controller, "show");
        get(name + "/new"     ).to(controller, "newForm");
        post(name + "/"       ).to(controller, "create");
        get(name + "/:id/edit").requires("id", "\\d+").to(controller, "edit");
        put(name + "/:id"     ).requires("id", "\\d+").to(controller, "update");
        delete(name + "/:id"   ).requires("id", "\\d+").to(controller, "delete");

        return null;
    }

    public Route resources(Class<?>... controllers) {
        Arrays.stream(controllers).map(c -> resource(c, null));
        return null;
    }

    public void namespace(String ns, RoutePatternsDescriptor subDesc) {
        /*
        RoutePatterns patterns = new RoutePatterns(new Routes());
        subDesc.describe(patterns);
        return patterns;
        */
        // TODO merge definitions.
    }


    private void addRoute_(Route route) {
        routeList.add(route);
    }

    private RouteBuilder getBuilder() {
        return builder;
    }


    public Routes compile() {
        routes.setRouteList(routeList);
        return routes;
    }

    static class PatternsContext {
        private RoutePatterns patterns;

        public PatternsContext(RoutePatterns routes) {
            this.patterns = routes;
        }

        public void addRoute(Route route) {
            patterns.addRoute_(route);
        }

        public Route build(String path, OptionMap options) {
            return patterns.getBuilder().build(path, options);
        }

    }
}
