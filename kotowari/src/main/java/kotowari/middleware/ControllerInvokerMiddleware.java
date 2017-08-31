package kotowari.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.collection.Parameters;
import enkan.data.*;
import enkan.exception.MisconfigurationException;
import enkan.security.UserPrincipal;
import enkan.system.inject.ComponentInjector;
import kotowari.data.BodyDeserializable;

import javax.enterprise.context.Conversation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static enkan.util.ReflectionUtils.tryReflection;

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
    Map<Class<?>, Object> controllerCache = new ConcurrentHashMap<>();
    ComponentInjector injector;

    public ControllerInvokerMiddleware(ComponentInjector injector) {
        this.injector = injector;
    }

    protected Object[] createArguments(HttpRequest request) {
        Method method = ((Routable) request).getControllerMethod();
        Object bodyObj = BodyDeserializable.class.cast(request).getDeserializedBody();
        Object[] arguments = new Object[method.getParameterCount()];

        // This code is quite ugly
        // TODO I think injectable type can be defined also out of this class.
        int parameterIndex = 0;
        for (Parameter parameter : method.getParameters()) {
            Class<?> type = parameter.getType();
            if (HttpRequest.class.isAssignableFrom(type)) {
                arguments[parameterIndex] = request;
            } else if (Session.class.isAssignableFrom(type)) {
                arguments[parameterIndex] = request.getSession();
            } else if (Flash.class.isAssignableFrom(type)) {
                arguments[parameterIndex] = request.getFlash();
            } else if (Parameters.class.isAssignableFrom(type)) {
                arguments[parameterIndex] = request.getParams();
            } else if (UserPrincipal.class.isAssignableFrom(type)) {
                arguments[parameterIndex] = PrincipalAvailable.class.cast(request).getPrincipal();
            } else if (Conversation.class.isAssignableFrom(type)) {
                arguments[parameterIndex] = request.getConversation();
            } else if (ConversationState.class.isAssignableFrom(type)) {
                ConversationState state = request.getConversationState();
                if (state == null) {
                    state = new ConversationState();
                    request.setConversationState(state);
                }
                arguments[parameterIndex] = state;
            } else if (bodyObj != null && bodyObj.getClass().equals(type)) {
                arguments[parameterIndex] = bodyObj;
            } else {
                throw new MisconfigurationException("PARAMETER_TYPE_MISMATCH",
                        method.getDeclaringClass().getSimpleName(),
                        method.getName(), parameterIndex, type);
            }
            parameterIndex++;
        }
        return arguments;
    }

    private Object inject(Object controller) {
        if (injector != null) {
            injector.inject(controller);
        }
        return controller;
    }

    @Override
    public RES handle(HttpRequest request, MiddlewareChain next) {
        if (request instanceof Routable) {
            Method controllerMethod = ((Routable) request).getControllerMethod();
            Class<?> controllerClass = controllerMethod.getDeclaringClass();

            Object controller = controllerCache.computeIfAbsent(controllerClass, c ->
                    tryReflection(() -> inject(c.newInstance())));

            return tryReflection(() -> {
                Object[] arguments = createArguments(request);
                return (RES) controllerMethod.invoke(controller, arguments);
            });
        } else {
            throw new MisconfigurationException("kotowari.MISSING_IMPLEMENTATION", Routable.class);
        }
    }
}
