package kotowari.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Headers;
import enkan.collection.OptionMap;
import enkan.collection.Parameters;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Routable;
import enkan.exception.MisconfigurationException;
import enkan.middleware.WebMiddleware;
import enkan.util.MixinUtils;
import enkan.util.ThreadingUtils;
import kotowari.component.TemplateEngine;
import kotowari.data.TemplatedHttpResponse;
import kotowari.routing.Routes;
import kotowari.routing.RoutingGenerationContext;
import kotowari.routing.UrlRewriter;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author kawasima
 */
@Middleware(name = "routing", mixins = Routable.class)
public class RoutingMiddleware implements WebMiddleware {
    @NotNull
    private Routes routes;

    @Inject
    private TemplateEngine<?> templateEngine;

    private final ConcurrentHashMap<String, Method> methodCache = new ConcurrentHashMap<>();

    public RoutingMiddleware(Routes routes) {
        this.routes = routes;
    }

    protected OptionMap recognizePath(HttpRequest request) {
        return routes.recognizePath(request);
    }

    @Override
    public <NNREQ, NNRES> HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, HttpResponse, NNREQ, NNRES> next) {
        request = MixinUtils.mixin(request, Routable.class);
        Class<?> controllerClass;

        OptionMap routing = recognizePath(request);
        if (routing.containsKey("controller")) {
            controllerClass = (Class<?>) routing.get("controller");
            String action = routing.getString("action");
            if (controllerClass != null) {
                ((Routable) request).setControllerClass(controllerClass);

                if (action != null) {
                    Method actionMethod = methodCache.computeIfAbsent(controllerClass.getName() + "#" + action, key -> Arrays.stream(controllerClass.getMethods())
                            .filter(m -> m.getName().equals(action))
                            .findAny()
                            .orElse(null));
                    ((Routable) request).setControllerMethod(actionMethod);
                }
            }


            if (controllerClass == null || (action != null && ((Routable) request).getControllerMethod() == null)) {
                HttpResponse response = HttpResponse.of("NotFound");
                response.setStatus(404);
                return response;
            }

            Parameters params = request.getParams();
            routing.keySet()
                    .stream()
                    .filter(k -> !k.equals("controller") && !k.equals("action"))
                    .forEach(k -> params.put(k, routing.getString(k)));
        } else {
            HttpResponse response =  HttpResponse.of("NotFound");
            response.setStatus(404);
            return response;
        }

        HttpResponse response = castToHttpResponse(next.next(request));
        if (response == null) return null;
        Headers headers = response.getHeaders();
        ThreadingUtils.some(headers.getRawType("Location"),
                loc -> {
                    if (loc instanceof RoutingGenerationContext rgc) {
                        headers.replace("Location", routes.generate(rgc.options()));
                        return headers;
                    }
                    return null;
                });
        if (response instanceof TemplatedHttpResponse templatedResponse) {
            Function<List<?>, Object> urlForFunction = arguments -> {
                if (arguments.isEmpty()) {
                    return "/";
                } else if (arguments.size() == 1){
                    return routes.generate(UrlRewriter.urlFor(controllerClass, arguments.getFirst().toString()).options());
                } else {
                    try {
                        Class<?> ctrlClass = Class.forName(arguments.get(0).toString(), true,
                                Thread.currentThread().getContextClassLoader());
                        return routes.generate(UrlRewriter.urlFor(ctrlClass, arguments.get(1).toString()).options());
                    } catch (ClassNotFoundException e) {
                        throw new MisconfigurationException("core.CLASS_NOT_FOUND", arguments.getFirst().toString(), e);
                    }
                }
            };
            templatedResponse.getContext().put("urlFor", templateEngine.createFunction(urlForFunction));
        }

        return response;
    }

    public Routes getRoutes() {
        return routes;
    }

    public void setRoutes(Routes routes) {
        this.routes = routes;
    }
}
