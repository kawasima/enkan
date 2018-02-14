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
import enkan.middleware.AbstractWebMiddleware;
import enkan.util.MixinUtils;
import enkan.util.ThreadingUtils;
import kotowari.component.TemplateEngine;
import kotowari.data.TemplatedHttpResponse;
import kotowari.routing.Routes;
import kotowari.routing.RoutingGenerationContext;
import kotowari.routing.UrlRewriter;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author kawasima
 */
@Middleware(name = "routing")
public class RoutingMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    @NotNull
    private Routes routes;

    @Inject
    private TemplateEngine templateEngine;

    public RoutingMiddleware(Routes routes) {
        this.routes = routes;
    }

    protected OptionMap recognizePath(HttpRequest request) {
        return routes.recognizePath(request);
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> next) {
        request = MixinUtils.mixin(request, Routable.class);
        Class<?> controllerClass;

        OptionMap routing = recognizePath(request);
        if (routing.containsKey("controller")) {
            controllerClass = (Class<?>) routing.get("controller");
            String action = routing.getString("action");
            Optional<Method> actionMethod = Arrays.stream(controllerClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals(action))
                    .filter(m -> Modifier.isPublic(m.getModifiers()))
                    .findAny();

            if (actionMethod.isPresent()) {
                ((Routable) request).setControllerMethod(actionMethod.get());
            } else {
                HttpResponse response =  HttpResponse.of("NotFound");
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
        Headers headers = response.getHeaders();
        ThreadingUtils.some(headers.getRawType("Location"),
                loc -> {
                    if (loc instanceof RoutingGenerationContext) {
                        headers.replace("Location", routes.generate(((RoutingGenerationContext) loc).getOptions()));
                        return headers;
                    }
                    return null;
                });
        if (response instanceof TemplatedHttpResponse) {
            Function<List, Object> urlForFunction = arguments -> {
                if (arguments.size() < 1) {
                    return "/";
                } else if (arguments.size() == 1){
                    return routes.generate(UrlRewriter.urlFor(controllerClass, arguments.get(0).toString()).getOptions());
                } else {
                    try {
                        Class<?> ctrlClass = Class.forName(arguments.get(0).toString(), true,
                                Thread.currentThread().getContextClassLoader());
                        return routes.generate(UrlRewriter.urlFor(ctrlClass, arguments.get(1).toString()).getOptions());
                    } catch (ClassNotFoundException e) {
                        throw new MisconfigurationException("core.CLASS_NOT_FOUND", arguments.get(0).toString(), e);
                    }
                }
            };
            ((TemplatedHttpResponse) response).getContext().put("urlFor", templateEngine.createFunction(urlForFunction));
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
