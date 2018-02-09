package kotowari.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.data.HttpRequest;
import enkan.data.Routable;
import enkan.exception.MisconfigurationException;
import enkan.system.inject.ComponentInjector;
import kotowari.inject.ParameterInjector;
import kotowari.inject.parameter.*;
import kotowari.util.ParameterUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static enkan.util.ReflectionUtils.*;

/**
 * Kotowari endpoint.
 *
 * This middleware inject arguments to controller method.
 *
 * <ul>
 *     <li>HttpRequest - Inject a request object.</li>
 *     <li>Map - Inject parsed request parameters.</li>
 *     <li>JavaBean - Inject form.</li>
 * </ul>
 *
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "controllerInvoker", dependencies = "params")
public class ControllerInvokerMiddleware<RES> implements Middleware<HttpRequest, RES> {
    private static final ParameterInjector<?> BODY_SERIALIZABLE_INJECTOR = new BodySerializableInjector<>();
    private final Map<Class<?>, Object> controllerCache = new ConcurrentHashMap<>();
    private final ComponentInjector componentInjector;
    private List<ParameterInjector<?>> parameterInjectors;

    public ControllerInvokerMiddleware(ComponentInjector componentInjector) {
        this.componentInjector = componentInjector;
    }

    @PostConstruct
    protected void setupParameterInjectors() {
        if (parameterInjectors == null) {
            parameterInjectors = ParameterUtils.getDefaultParameterInjectors();
        }
    }

    protected Object[] createArguments(HttpRequest request) {
        Method method = ((Routable) request).getControllerMethod();
        Object[] arguments = new Object[method.getParameterCount()];

        int i = 0;
        for (Parameter parameter : method.getParameters()) {
            Class<?> type = parameter.getType();
            final int parameterIndex = i;
            ParameterInjector<?> parameterInjector = parameterInjectors.stream()
                    .filter(injector -> injector.isApplicable(type, request))
                    .findAny()
                    .orElse(BODY_SERIALIZABLE_INJECTOR);

            arguments[parameterIndex] = parameterInjector.getInjectObject(request);
            i++;
        }
        return arguments;
    }

    private Object inject(Object controller) {
        if (componentInjector != null) {
            componentInjector.inject(controller);
        }
        return controller;
    }

    @Override
    public RES handle(HttpRequest request, MiddlewareChain next) {
        if (request instanceof Routable) {
            Method controllerMethod = ((Routable) request).getControllerMethod();
            Class<?> controllerClass = controllerMethod.getDeclaringClass();

            Object controller = controllerCache.computeIfAbsent(controllerClass, c ->
                    tryReflection(() -> inject(c.getConstructor().newInstance())));

            return tryReflection(() -> {
                Object[] arguments = createArguments(request);
                return (RES) controllerMethod.invoke(controller, arguments);
            });
        } else {
            throw new MisconfigurationException("kotowari.MISSING_IMPLEMENTATION", Routable.class);
        }
    }

    public void setParameterInjectors(List<ParameterInjector<?>> parameterInjectors) {
        this.parameterInjectors = parameterInjectors;
    }
}
