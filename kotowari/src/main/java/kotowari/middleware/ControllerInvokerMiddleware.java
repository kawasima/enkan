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

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static enkan.util.ReflectionUtils.*;

/**
 * Kotowari endpoint.
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
public class ControllerInvokerMiddleware<RES> implements Middleware<HttpRequest, RES, Void, Void> {
    private final Map<Class<?>, Object> controllerCache = new ConcurrentHashMap<>();
    private final Map<Method, ParameterInjector<?>[]> injectorCache = new ConcurrentHashMap<>();
    private final ComponentInjector componentInjector;
    private static final ParameterInjector<?> BODY_SERIALIZABLE_INJECTOR = new BodySerializableInjector<>();
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

    /**
     * Resolve injectors for each parameter of the given method by type only.
     *
     * <p>All built-in {@link ParameterInjector#isApplicable} implementations
     * determine applicability solely from the parameter type, so passing
     * {@code null} for the request is safe.  Custom injectors that rely on
     * request state at applicability-check time are not supported by this
     * caching strategy and must be handled separately.
     */
    private ParameterInjector<?>[] resolveInjectors(Method method) {
        Parameter[] parameters = method.getParameters();
        ParameterInjector<?>[] injectors = new ParameterInjector<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> type = parameters[i].getType();
            injectors[i] = parameterInjectors.stream()
                    .filter(injector -> injector.isApplicable(type, null))
                    .findFirst()
                    .orElse(BODY_SERIALIZABLE_INJECTOR);
        }
        return injectors;
    }

    /**
     * Create arguments for controller method.
     *
     * @param request the request object
     * @return arguments for controller method
     */
    protected Object[] createArguments(HttpRequest request) {
        Method method = ((Routable) request).getControllerMethod();
        ParameterInjector<?>[] injectors = injectorCache.computeIfAbsent(method, this::resolveInjectors);
        Object[] arguments = new Object[injectors.length];
        for (int i = 0; i < injectors.length; i++) {
            arguments[i] = injectors[i].getInjectObject(request);
        }
        return arguments;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <NNREQ, NNRES> RES handle(HttpRequest request, MiddlewareChain<Void, Void, NNREQ, NNRES> next) {
        if (request instanceof Routable routable) {
            Method controllerMethod = routable.getControllerMethod();
            Class<?> controllerClass = controllerMethod.getDeclaringClass();

            Object controller = controllerCache.computeIfAbsent(controllerClass, c ->
                    componentInjector != null ? componentInjector.newInstance(c)
                            : tryReflection(() -> c.getConstructor().newInstance()));

            return (RES) tryReflection(() -> {
                Object[] arguments = createArguments(request);
                return controllerMethod.invoke(controller, arguments);
            });
        } else {
            throw new MisconfigurationException("kotowari.MISSING_IMPLEMENTATION", Routable.class);
        }
    }

    public void setParameterInjectors(List<ParameterInjector<?>> parameterInjectors) {
        this.parameterInjectors = parameterInjectors;
    }
}
