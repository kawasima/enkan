package enkan.component;

/**
 * @author kawasima
 */
public class LifecycleManager {
    public static void start(SystemComponent component) {
        component.lifecycle().start(component);
    }

    public static void stop(SystemComponent component) {
        component.lifecycle().stop(component);
    }
}
