package enkan.system.inject;

import enkan.component.SystemComponent;
import enkan.exception.UnreachableException;
import enkan.exception.UnrecoverableException;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

/**
 * @author kawasima
 */
public class ComponentInjector {
    private Map<String, SystemComponent> components;

    public ComponentInjector(Map<String, SystemComponent> components) {
        this.components = components;
    }

    private boolean isInjectTarget(Field f) {
        return f.getAnnotation(Inject.class) != null;
    }

    private void setValueToField(Object target, Field f, Object value) {
        if (!f.isAccessible()) {
            f.setAccessible(true);
        }
        try {
            f.set(target, value);
        } catch (IllegalAccessException e) {
            throw UnreachableException.create(e);
        }
    }

    protected void injectField(Object target, Field f) {
        Named named = f.getAnnotation(Named.class);
        if (named != null) {
            setValueToField(target, f, components.get(named.value()));
        } else {
            components.values().stream()
                    .filter(component -> f.getType().isAssignableFrom(component.getClass()))
                    .findFirst()
                    .ifPresent(c -> setValueToField(target, f, c));
        }
    }

    public void inject(Object target) {
        Arrays.stream(target.getClass().getDeclaredFields())
                .filter(f -> isInjectTarget(f))
                .forEach(f -> injectField(target, f));
    }
}
