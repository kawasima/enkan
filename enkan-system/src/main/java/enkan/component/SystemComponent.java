package enkan.component;

import enkan.exception.MisconfigurationException;

import java.util.Map;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new MisconfigurationException("core.CLASS_NOT_FOUND", componentClass));
    }

    protected Map<String, SystemComponent> getAllDependencies() {
        return dependencies;
    }

    protected abstract ComponentLifecycle lifecycle();

    public String dependenciesToString() {
        String s = (dependencies == null) ? "" :
                dependencies.keySet().stream()
                        .map(name -> "\"" + name + "\"")
                        .collect(Collectors.joining(", "));
        return "[" + s  + "]";
    }
}
