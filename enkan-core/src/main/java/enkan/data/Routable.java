package enkan.data;

import java.lang.reflect.Method;

/**
 * Holds the controller method.
 *
 * @author kawasima
 */
public interface Routable extends Extendable {
    default Method getControllerMethod() {
        return (Method) getExtension("controllerMethod");
    }

    default void setControllerMethod(Method controllerMethod) {
        setExtension("controllerMethod", controllerMethod);
    }
}
