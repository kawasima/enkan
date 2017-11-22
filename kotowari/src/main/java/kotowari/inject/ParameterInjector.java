package kotowari.inject;

import enkan.data.HttpRequest;

/**
 * Inject object to a controller method.
 *
 * @param <T> The type of Inject object.
 * @author kawasima
 */
public interface ParameterInjector<T> {
    String getName();

    boolean isApplicable(Class<?> type, HttpRequest request);

    T getInjectObject(HttpRequest request);
}
