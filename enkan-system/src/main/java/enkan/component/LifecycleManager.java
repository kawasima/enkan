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
    public static void start(SystemComponent component) {
        component.lifecycle().start(component);
    }

    /**
     * Stops the component.
     *
     * @param component the given component
     */
    public static void stop(SystemComponent component) {
        component.lifecycle().stop(component);
    }
}
