package enkan.component;

import enkan.exception.UnrecoverableException;

import java.util.Map;

/**
 * @author kawasima
 */
public abstract class SystemComponent {
    private Map<String, SystemComponent> dependencies;

    public void setDependencies(Map<String, SystemComponent> dependencies) {
        this.dependencies = dependencies;
    }

    public <T extends SystemComponent> T getDependency(Class<T> componentClass) {
        return (T) dependencies.values().stream()
                .filter(c -> componentClass.isAssignableFrom(c.getClass()))
                .findFirst()
                .orElseThrow(() -> UnrecoverableException.create("Can't find component [" + componentClass + "]"));
    }

    protected Map<String, SystemComponent> getAllDependencies() {
        return dependencies;
    }

    protected abstract ComponentLifecycle lifecycle();
}
