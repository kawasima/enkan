package enkan.component;

import enkan.exception.MisconfigurationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract base class for all system components in the Enkan framework.
 * This class provides core functionality for component dependency management
 * and lifecycle control.
 *
 * <p>System components are the building blocks of the Enkan application system.
 * Each component can have dependencies on other components and manages its own
 * lifecycle through the {@link ComponentLifecycle} interface.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *     <li>Dependency management between components</li>
 *     <li>Lifecycle control (start/stop)</li>
 *     <li>Type-safe dependency injection</li>
 * </ul>
 *
 * @param <T> The concrete type of the component, used for type-safe lifecycle management
 * @author kawasima
 */
public abstract class SystemComponent<T extends SystemComponent>  {
    private Map<String, SystemComponent> dependencies = new HashMap<>();

    /**
     * Sets the dependencies for this component.
     * This method is used by the system to inject component dependencies.
     *
     * @param dependencies Map of component name to component instance
     */
    protected void setDependencies(Map<String, SystemComponent> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Retrieves a dependency of the specified type.
     * This method provides type-safe access to component dependencies.
     *
     * @param componentClass The class of the required component
     * @param <S> The type of the component to retrieve
     * @return The component instance of the specified type
     * @throws MisconfigurationException if no component of the specified type is found
     */
    @SuppressWarnings("unchecked")
    protected <S extends SystemComponent> S getDependency(Class<S> componentClass) {
        return (S) dependencies.values().stream()
                .filter(c -> componentClass.isAssignableFrom(c.getClass()))
                .findAny()
                .orElseThrow(() -> new MisconfigurationException("core.CLASS_NOT_FOUND", componentClass));
    }

    /**
     * Returns all dependencies of this component.
     *
     * @return Map of component name to component instance
     */
    protected Map<String, SystemComponent> getAllDependencies() {
        return dependencies;
    }

    /**
     * Returns the lifecycle manager for this component.
     * Each component must implement this method to define its lifecycle behavior.
     *
     * @return The lifecycle manager for this component
     */
    protected abstract ComponentLifecycle<T> lifecycle();

    /**
     * Creates a string representation of this component's dependencies.
     * Used for debugging and logging purposes.
     *
     * @return A JSON-style array string containing dependency names
     */
    protected String dependenciesToString() {
        String s = (dependencies == null) ? "" :
                dependencies.keySet().stream()
                        .map(name -> "\"" + name + "\"")
                        .collect(Collectors.joining(", "));
        return "[" + s  + "]";
    }
}
