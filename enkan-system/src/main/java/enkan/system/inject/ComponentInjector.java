package enkan.system.inject;

import enkan.component.SystemComponent;
import enkan.exception.MisconfigurationException;
import enkan.exception.UnreachableException;
import enkan.util.SearchUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import static enkan.util.ReflectionUtils.tryReflection;
import static enkan.util.SearchUtils.levenshteinDistance;

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
            String name = named.value();
            SystemComponent component = components.get(name);
            if (component != null) {
                setValueToField(target, f, component);
            } else {
                Optional<String> correctName = components.entrySet().stream()
                        .filter(c -> f.getType().isAssignableFrom(c.getValue().getClass()))
                        .map(Map.Entry::getKey)
                        .sorted(Comparator.comparing(n -> levenshteinDistance(n, name)))
                        .findFirst();
                if (correctName.isPresent()) {
                    throw MisconfigurationException.create("INJECT_WRONG_NAMED_COMPONENT", name, correctName.get());
                } else {
                    throw MisconfigurationException.create("INJECT_WRONG_TYPE_COMPONENT", name, f.getType());
                }
            }
        } else {
            components.values().stream()
                    .filter(component -> f.getType().isAssignableFrom(component.getClass()))
                    .findFirst()
                    .ifPresent(c -> setValueToField(target, f, c));
        }
    }

    protected <T> void postConstruct(T target) {
        Arrays.stream(target.getClass().getDeclaredMethods())
                .filter(m -> m.getAnnotation(PostConstruct.class) != null)
                .findFirst()
                .ifPresent(m -> tryReflection(
                        () -> {
                            m.setAccessible(true);
                            return m.invoke(target);
                        })
                );
    }

    public <T> T inject(T target) {
        Arrays.stream(target.getClass().getDeclaredFields())
                .filter(this::isInjectTarget)
                .forEach(f -> injectField(target, f));
        postConstruct(target);
        return target;
    }
}
