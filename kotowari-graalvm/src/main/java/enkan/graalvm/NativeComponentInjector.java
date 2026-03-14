package enkan.graalvm;

import enkan.component.SystemComponent;
import enkan.system.inject.ComponentInjector;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Arrays;
import java.util.Map;

/**
 * A {@link ComponentInjector} subclass that uses pre-generated {@link ComponentBinder}
 * instances for component creation instead of reflection.
 *
 * <p>For each component class registered in {@link NativeComponentRegistry}, this injector
 * delegates to the generated binder (direct field writes, zero reflection at runtime) for
 * {@code @Named @Inject} fields, then falls back to {@link #inject} for any remaining
 * unnamed {@code @Inject} fields and invokes {@code @PostConstruct}.
 * For any class without a registered binder, it falls back entirely to the standard
 * reflection-based {@code ComponentInjector}.
 *
 * <p><b>Limitation:</b> unnamed {@code @Inject} fields (those annotated with
 * {@code @Inject} but <em>not</em> with {@code @Named}) are resolved by
 * {@link ComponentInjector#injectField} which calls {@code Field.setAccessible(true)}.
 * Any component class that has such fields must therefore have
 * {@code "allDeclaredFields": true} in its {@code reflect-config.json} entry,
 * or be registered via {@code EnkanFeature} with
 * {@code RuntimeReflection.registerAllFields(componentClass)}.
 * Components that rely only on {@code @Named @Inject} fields (the common case for
 * named component dependencies) require no additional reflection registration.
 */
public class NativeComponentInjector extends ComponentInjector {
    private final Map<String, SystemComponent<?>> components;

    public NativeComponentInjector(Map<String, SystemComponent<?>> components) {
        super(components);
        this.components = components;
    }

    @Override
    public <T> T newInstance(Class<T> clazz) {
        ComponentBinder<T> binder = NativeComponentRegistry.get(clazz);
        if (binder == null) {
            return super.newInstance(clazz);
        }

        // binder handles @Named @Inject fields via direct putfield (no reflection).
        // Inject any remaining unnamed @Inject fields via the reflection fallback,
        // then call @PostConstruct.
        T instance = binder.bind(components);
        Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.getAnnotation(Inject.class) != null
                          && f.getAnnotation(Named.class) == null)
                .forEach(f -> injectField(instance, f));
        postConstruct(instance);
        return instance;
    }
}
