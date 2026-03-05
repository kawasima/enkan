package enkan.system.inject;

import enkan.component.SystemComponent;
import enkan.exception.MisconfigurationException;
import enkan.exception.UnreachableException;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.lang.reflect.Constructor;
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
    private final Map<String, SystemComponent<?>> components;

    public ComponentInjector(Map<String, SystemComponent<?>> components) {
        this.components = components;
    }

    private boolean isInjectTarget(Field f) {
        return f.getAnnotation(Inject.class) != null;
    }

    /**
     * Set a value to a field of an object.
     * Notes: Field#isAccessible method is deprecated from Java9.
     *
     * @param target an injection target object
     * @param f      an injection target field
     * @param value  an injecting value
     */
    @SuppressWarnings("deprecation")
    private void setValueToField(Object target, Field f, Object value) {
        if (!f.isAccessible()) {
            f.setAccessible(true);
        }
        try {
            f.set(target, value);
        } catch (IllegalAccessException e) {
            throw new UnreachableException(e);
        }
    }

    protected void injectField(Object target, Field f) {
        Named named = f.getAnnotation(Named.class);
        if (named != null) {
            String name = named.value();
            SystemComponent<?> component = components.get(name);
            if (component != null) {
                setValueToField(target, f, component);
            } else {
                Optional<String> correctName = components.entrySet().stream()
                        .filter(c -> f.getType().isAssignableFrom(c.getValue().getClass()))
                        .map(Map.Entry::getKey)
                        .min(Comparator.comparing(n -> levenshteinDistance(n, name)));
                if (correctName.isPresent()) {
                    throw new MisconfigurationException("core.INJECT_WRONG_NAMED_COMPONENT", name, correctName.get());
                } else {
                    throw new MisconfigurationException("core.INJECT_WRONG_TYPE_COMPONENT", name, f.getType());
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

    /**
     * Create a new instance of the given class using constructor injection if available.
     *
     * <p>If the class has a constructor annotated with {@code @Inject}, its parameters
     * are resolved by type from the registered components. Otherwise, the default
     * constructor is used and field injection is applied.
     *
     * @param clazz the class to instantiate
     * @param <T>   the type of the class
     * @return a new instance with dependencies injected
     */
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<T> clazz) {
        Constructor<T> injectConstructor = findInjectConstructor(clazz);
        if (injectConstructor != null) {
            Object[] args = resolveConstructorArgs(injectConstructor);
            T instance = tryReflection(() -> injectConstructor.newInstance(args));
            postConstruct(instance);
            return instance;
        }
        // Fallback: default constructor + field injection
        T instance = tryReflection(() -> clazz.getConstructor().newInstance());
        return inject(instance);
    }

    @SuppressWarnings("unchecked")
    private <T> Constructor<T> findInjectConstructor(Class<T> clazz) {
        return (Constructor<T>) Arrays.stream(clazz.getDeclaredConstructors())
                .filter(c -> c.getAnnotation(Inject.class) != null)
                .findFirst()
                .orElse(null);
    }

    private Object[] resolveConstructorArgs(Constructor<?> constructor) {
        java.lang.reflect.Parameter[] params = constructor.getParameters();
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Class<?> type = params[i].getType();
            Named named = params[i].getAnnotation(Named.class);
            if (named != null) {
                SystemComponent<?> component = components.get(named.value());
                if (component != null && type.isAssignableFrom(component.getClass())) {
                    args[i] = component;
                } else {
                    throw new MisconfigurationException("core.INJECT_WRONG_NAMED_COMPONENT",
                            named.value(), suggestName(type, named.value()));
                }
            } else {
                args[i] = components.values().stream()
                        .filter(component -> type.isAssignableFrom(component.getClass()))
                        .findFirst()
                        .orElse(null);
            }
        }
        return args;
    }

    private String suggestName(Class<?> type, String wrongName) {
        return components.entrySet().stream()
                .filter(c -> type.isAssignableFrom(c.getValue().getClass()))
                .map(Map.Entry::getKey)
                .min(Comparator.comparing(n -> levenshteinDistance(n, wrongName)))
                .orElse("(no matching component)");
    }
}
