package enkan.data;

import java.lang.reflect.Method;

/**
 * Holds the controller method.
 *
 * @author kawasima
 */
public interface Routable extends Extendable {
    default Class<?> getControllerClass() {
        return getExtension("controllerClass");
    }

    default void setControllerClass(Class<?> controllerClass) {
        setExtension("controllerClass", controllerClass);
    }

    default Method getControllerMethod() {
        return getExtension("controllerMethod");
    }
    default void setControllerMethod(Method controllerMethod) {
        setExtension("controllerMethod", controllerMethod);
    }

    default String getControllerMethodName() {
        return getExtension("controllerMethodName");
    }

    default void setControllerMethodName(String name) {
        setExtension("controllerMethodName", name);
    }
}
