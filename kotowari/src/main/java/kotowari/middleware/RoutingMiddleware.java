package kotowari.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.AbstractWebMiddleware;
import enkan.util.MixinUtils;
import enkan.data.Routable;
import kotowari.routing.Routes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * @author kawasima
 */
@Middleware(name = "routing")
public class RoutingMiddleware extends AbstractWebMiddleware {
    private Routes routes;

    public RoutingMiddleware(Routes routes) {
        this.routes = routes;
    }
    protected OptionMap recognizePath(HttpRequest request) {
        return routes.recognizePath(request.getUri(), request.getRequestMethod().toUpperCase(Locale.US));
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        request = MixinUtils.mixin(request, Routable.class);

        OptionMap routing = recognizePath(request);
        if (routing.containsKey("controller")) {
            Class<?> controllerClass = (Class<?>) routing.get("controller");
            String action = routing.getString("action");
            Optional<Method> actionMethod = Arrays.stream(controllerClass.getMethods())
                    .filter(m -> m.getName().equals(action))
                    .findFirst();


            if (actionMethod.isPresent()) {
                ((Routable) request).setControllerMethod(actionMethod.get());
            } else {
                HttpResponse response =  HttpResponse.of("NotFound");
                response.setStatus(404);
                return response;
            }
        } else {
            HttpResponse response =  HttpResponse.of("");
            response.setStatus(404);
            return response;
        }

        return castToHttpResponse(next.next(request));
    }

    public Routes getRoutes() {
        return routes;
    }
}
