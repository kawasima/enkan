package enkan.util;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates and caches ByteBuddy subclasses for mixin interface injection.
 *
 * <p>Instead of creating JDK dynamic proxies, this generator produces concrete
 * subclasses that implement the requested mixin interfaces. Since all mixin
 * interfaces use {@code default} methods delegating to
 * {@link enkan.data.Extendable#getExtension}/{@link enkan.data.Extendable#setExtension},
 * no method body generation is needed — normal inheritance resolves everything.
 *
 * @author kawasima
 */
class MixinClassGenerator {
    private static final ConcurrentHashMap<List<Class<?>>, Class<?>> classCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, FieldCopier> copierCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Constructor<?>> constructorCache = new ConcurrentHashMap<>();

    /**
     * Returns a cached ByteBuddy-generated class that extends {@code concreteClass}
     * and implements all interfaces in {@code allInterfaces}.
     */
    static Class<?> getOrCreateClass(Class<?> concreteClass, Class<?>[] allInterfaces) {
        List<Class<?>> cacheKey = buildKey(concreteClass, allInterfaces);
        return classCache.computeIfAbsent(cacheKey, k -> generateClass(concreteClass, allInterfaces));
    }

    /**
     * Creates a new instance of {@code generatedClass} and copies all field values
     * from {@code original}.
     */
    @SuppressWarnings("unchecked")
    static <T> T copyInto(Class<?> generatedClass, T original) {
        try {
            Constructor<?> ctor = constructorCache.computeIfAbsent(generatedClass,
                    MixinClassGenerator::resolveConstructor);
            T instance = (T) ctor.newInstance();
            FieldCopier copier = copierCache.computeIfAbsent(original.getClass(),
                    MixinClassGenerator::buildCopier);
            copier.copy(original, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create mixin instance", e);
        }
    }

    private static Class<?> generateClass(Class<?> concreteClass, Class<?>[] interfaces) {
        // Collect only interfaces not already implemented by concreteClass.
        // MixinGenerated is handled separately — always added once.
        List<Class<?>> allIfaces = new ArrayList<>(interfaces.length + 1);
        for (Class<?> iface : interfaces) {
            if (iface == MixinGenerated.class) continue;
            if (!iface.isAssignableFrom(concreteClass) && !allIfaces.contains(iface)) {
                allIfaces.add(iface);
            }
        }
        allIfaces.add(MixinGenerated.class);

        return new ByteBuddy()
                .subclass(concreteClass)
                .implement(allIfaces)
                .make()
                .load(concreteClass.getClassLoader(),
                        ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
    }

    private static Constructor<?> resolveConstructor(Class<?> clazz) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No no-arg constructor on " + clazz.getName(), e);
        }
    }

    private static FieldCopier buildCopier(Class<?> clazz) {
        List<FieldHandle> handles = new ArrayList<>();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                try {
                    MethodHandle getter = lookup.unreflectGetter(field);
                    MethodHandle setter = lookup.unreflectSetter(field);
                    handles.add(new FieldHandle(getter, setter));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot access field " + field, e);
                }
            }
            current = current.getSuperclass();
        }
        return new FieldCopier(handles.toArray(new FieldHandle[0]));
    }

    private static List<Class<?>> buildKey(Class<?> concreteClass, Class<?>[] interfaces) {
        Class<?>[] key = new Class<?>[1 + interfaces.length];
        key[0] = concreteClass;
        System.arraycopy(interfaces, 0, key, 1, interfaces.length);
        return Arrays.asList(key);
    }

    private record FieldHandle(MethodHandle getter, MethodHandle setter) {
        void copy(Object from, Object to) throws Throwable {
            setter.invoke(to, getter.invoke(from));
        }
    }

    static class FieldCopier {
        private final FieldHandle[] handles;

        FieldCopier(FieldHandle[] handles) {
            this.handles = handles;
        }

        void copy(Object from, Object to) {
            try {
                for (FieldHandle h : handles) {
                    h.copy(from, to);
                }
            } catch (Throwable e) {
                throw new RuntimeException("Field copy failed", e);
            }
        }
    }
}
