package kotowari.inject;

import enkan.data.HttpRequest;

/**
 * Inject object to a controller method.
 *
 * @param <T> The type of Inject object.
 * @author kawasima
 */
public interface ParameterInjector<T> {
    /**
     * Get the name of the injector.
     *
     * @return the name of the injector
     */
    String getName();

    /**
     * Whether the injector can inject the given type.
     *
     * @param type the class of a parameter
     * @param request the request object
     * @return true if the injector can inject the given type.
     */
    boolean isApplicable(Class<?> type, HttpRequest request);

    /**
     * Get a object for injecting.
     *
     * @param request the request object
     * @return an object for injection
     */
    T getInjectObject(HttpRequest request);
}
