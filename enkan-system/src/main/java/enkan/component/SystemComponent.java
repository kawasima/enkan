package enkan.component;

import enkan.exception.MisconfigurationException;

import java.util.Map;

/**
 * @author kawasima
 */
public abstract class SystemComponent {
    private Map<String, SystemComponent> dependencies;

    protected void setDependencies(Map<String, SystemComponent> dependencies) {
        this.dependencies = dependencies;
    }

    protected <T extends SystemComponent> T getDependency(Class<T> componentClass) {
        return (T) dependencies.values().stream()
                .filter(c -> componentClass.isAssignableFrom(c.getClass()))
                .findFirst()
                .orElseThrow(() -> MisconfigurationException.create("CLASS_NOT_FOUND", componentClass));
    }

    protected Map<String, SystemComponent> getAllDependencies() {
        return dependencies;
    }

    protected abstract ComponentLifecycle lifecycle();
}
