package enkan.component;

/**
 * Starts and stops the component.
 *
 * @author kawasima
 */
public class LifecycleManager {
    /**
     * Starts the component.
     *
     * @param component the given component
     */
    public static <T extends SystemComponent<T>> void start(T component) {
        component.lifecycle().start(component);
    }

    /**
     * Stops the component.
     *
     * @param component the given component
     */
    public static <T extends SystemComponent<T>> void stop(T component) {
        component.lifecycle().stop(component);
    }
}
