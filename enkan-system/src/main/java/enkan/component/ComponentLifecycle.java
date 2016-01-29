package enkan.component;

/**
 * @author kawasima
 */
public interface ComponentLifecycle<T extends SystemComponent> {
    void start(T component);
    void stop(T component);
}
