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

    @SuppressWarnings("unchecked")
    protected <S extends SystemComponent> S getDependency(Class<S> componentClass) {
        return (S) dependencies.values().stream()
                .filter(c -> componentClass.isAssignableFrom(c.getClass()))
                .findAny()
                .orElseThrow(() -> new MisconfigurationException("core.CLASS_NOT_FOUND", componentClass));
    }

    protected Map<String, SystemComponent> getAllDependencies() {
        return dependencies;
    }

    protected abstract <T extends SystemComponent> ComponentLifecycle<T> lifecycle();

    protected String dependenciesToString() {
        String s = (dependencies == null) ? "" :
                dependencies.keySet().stream()
                        .map(name -> "\"" + name + "\"")
                        .collect(Collectors.joining(", "));
        return "[" + s  + "]";
    }
}
