package enkan.component;

import enkan.component.SystemComponent;

/**
 * @author kawasima
 */
public interface ComponentLifecycle<T extends SystemComponent> {
    void start(T component);
    void stop(T component);
}
