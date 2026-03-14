package enkan.graalvm;

import enkan.component.SystemComponent;

import java.util.Map;

/**
 * Generated at GraalVM native image build time by {@link EnkanFeature} for each
 * registered component class.  Implementations use direct field writes ({@code putfield})
 * and direct method calls ({@code invokevirtual}) instead of reflection, eliminating
 * {@link java.lang.reflect.Field#set} and {@link java.lang.reflect.Method#invoke} at runtime.
 *
 * @param <T> the component type
 */
public interface ComponentBinder<T> {
    /**
     * Create a new component instance and inject its dependencies from the provided map.
     *
     * @param components all registered components keyed by name
     * @return a fully-configured component instance
     */
    T bind(Map<String, SystemComponent<?>> components);
}
