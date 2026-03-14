package enkan.graalvm;

import enkan.component.SystemComponent;
import enkan.system.inject.ComponentInjector;

import java.util.Map;

/**
 * A {@link ComponentInjector} subclass that uses pre-generated {@link ComponentBinder}
 * instances for component creation instead of reflection.
 *
 * <p>For each component class registered in {@link NativeComponentRegistry}, this injector
 * delegates to the generated binder (direct field writes, zero reflection at runtime).
 * For any class without a registered binder, it falls back to the standard reflection-based
 * {@code ComponentInjector} so the JVM development path is unaffected.
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
        if (binder != null) {
            return binder.bind(components);
        }
        return super.newInstance(clazz);
    }
}
