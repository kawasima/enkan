package enkan.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static enkan.util.ReflectionUtils.tryReflection;

/**
 * Utility class for dynamically augmenting objects with additional interfaces
 * at runtime using JDK dynamic proxies.
 *
 * <p>Enkan middleware frequently needs to attach capabilities to a request
 * object — for example, marking it as {@code EntityManageable} or
 * {@code BodyDeserializable} — without knowing its concrete type at compile
 * time.  {@code MixinUtils.mixin(target, Interface.class)} wraps {@code target}
 * in a proxy that:
 * <ol>
 *   <li>Forwards calls declared by the original class to the original object.</li>
 *   <li>Delegates calls declared by the newly added interface(s) to their
 *       {@code default} implementations, which typically store/retrieve data
 *       via the {@link enkan.data.Extendable} property bag.</li>
 * </ol>
 *
 * <p>Subsequent calls to {@code mixin} on an already-proxied object reuse the
 * same underlying instance and simply extend the proxy's interface set, so the
 * cost of layering multiple interfaces is minimal.
 *
 * @author kawasima
 */
public class MixinUtils {
    /** Cache for default-method handles (unreflectSpecial). */
    private static final ConcurrentHashMap<Method, MethodHandle> methodHandleCache = new ConcurrentHashMap<>();
    /** Cache for original-object delegation handles (unreflect). */
    private static final ConcurrentHashMap<Method, MethodHandle> delegateHandleCache = new ConcurrentHashMap<>();
    /** Cache: (originalClass, mixinInterfaces) → merged interface array for Proxy. */
    private static final ConcurrentHashMap<List<Class<?>>, Class<?>[]> interfaceArrayCache = new ConcurrentHashMap<>();
    /** Cache: class → all implemented interfaces (avoids repeated class hierarchy traversal). */
    private static final ConcurrentHashMap<Class<?>, Class<?>[]> allInterfacesCache = new ConcurrentHashMap<>();

    /**
     * Build a MethodHandle for a default method, normalized to
     * {@code (Object, Object[]) -> Object} via asSpreader.
     */
    private static MethodHandle lookupSpecial(Method m) {
        Class<?> declaringClass = m.getDeclaringClass();
        MethodHandle mh = tryReflection(() -> {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup());
            return lookup.unreflectSpecial(m, declaringClass);
        });
        int paramCount = m.getParameterCount();
        return mh.asType(MethodType.genericMethodType(1 + paramCount))
                  .asSpreader(Object[].class, paramCount);
    }

    /**
     * Build a MethodHandle for delegating to the original object, normalized
     * to the shape {@code (Object, Object[]) -> Object} via asSpreader so
     * the hot path avoids per-call array allocation.
     */
    private static MethodHandle lookupDelegate(Method m) {
        MethodHandle mh;
        try {
            mh = MethodHandles.publicLookup().unreflect(m);
        } catch (IllegalAccessException e) {
            mh = tryReflection(() -> {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                        m.getDeclaringClass(), MethodHandles.lookup());
                return lookup.unreflect(m);
            });
        }
        // Normalize to (Object, Object[]) -> Object so invoke() at call
        // site needs no array copy.
        int paramCount = m.getParameterCount();
        return mh.asType(MethodType.genericMethodType(1 + paramCount))
                  .asSpreader(Object[].class, paramCount);
    }

    static MethodHandle getMethodHandle(Method method) {
        return  methodHandleCache.computeIfAbsent(method, MixinUtils::lookupSpecial);
    }

    static MethodHandle getDelegateHandle(Method method) {
        return delegateHandleCache.computeIfAbsent(method, MixinUtils::lookupDelegate);
    }

    record MixinProxyHandler<T>(T original, Class<?>[] proxyInterfaces) implements InvocationHandler {


        @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass().isAssignableFrom(original.getClass())) {
                    if (method.getName().equals("equals") && args.length == 1) {
                        return args[0] == proxy;
                    } else {
                        // Handle is pre-shaped to (Object, Object[]) via asSpreader
                        return getDelegateHandle(method)
                                .invoke(original, args != null ? args : new Object[0]);
                    }
                } else {
                    // Handle is pre-shaped to (Object, Object[]) via asSpreader
                    return getMethodHandle(method)
                            .invoke(proxy, args != null ? args : new Object[0]);
                }
            }
        }

    private static void getAllInterfaces(Class<?> clazz, final LinkedHashSet<Class<?>> interfacesFound) {
        while (clazz != null) {
            final Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> i : interfaces) {
                if (interfacesFound.add(i)) {
                    getAllInterfaces(i, interfacesFound);
                }
            }

            clazz = clazz.getSuperclass();
        }

    }

    static Class<?>[] getAllInterfaces(final Class<?> clazz) {
        if (clazz == null) return null;
        return allInterfacesCache.computeIfAbsent(clazz, c -> {
            final LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<>();
            getAllInterfaces(c, interfacesFound);
            return interfacesFound.toArray(new Class<?>[0]);
        });
    }

    /**
     * Returns a proxy that implements all interfaces currently implemented by
     * {@code target} <em>plus</em> the supplied {@code interfaces}.
     *
     * <p>If {@code target} is already a mixin proxy the new interfaces are
     * merged into the existing proxy rather than creating a nested proxy.
     * If {@code target} already implements every requested interface, it is
     * returned unchanged without creating a proxy.
     *
     * @param <T>        the type of the target object
     * @param target     the object to augment; {@code null} is returned as-is
     * @param interfaces one or more interfaces to add to the proxy
     * @return a proxy implementing all original interfaces plus the requested
     *         ones, or {@code target} itself if no new interfaces are needed
     */
    @SuppressWarnings("unchecked")
    public static <T> T mixin(T target, Class<?>... interfaces) {
        if (target == null) return null;

        final Class<?> targetClass = target.getClass();
        boolean allPresent = true;
        for (Class<?> iface : interfaces) {
            if (!iface.isAssignableFrom(targetClass)) {
                allPresent = false;
                break;
            }
        }
        if (allPresent) {
            return target;
        }

        // Extract the original object and its current proxy interfaces
        Class<?>[] currentInterfaces;
        if (Proxy.isProxyClass(targetClass)) {
            MixinProxyHandler<T> handler = ((MixinProxyHandler<T>) Proxy.getInvocationHandler(target));
            target = handler.original();
            currentInterfaces = handler.proxyInterfaces();
        } else {
            currentInterfaces = getAllInterfaces(targetClass);
        }

        // Build cache key: [originalClass, currentInterfaces..., newInterfaces...]
        // Use List for proper equals/hashCode
        List<Class<?>> cacheKey = buildCacheKey(target.getClass(), currentInterfaces, interfaces);
        Class<?>[] classes = interfaceArrayCache.computeIfAbsent(cacheKey, k -> {
            Class<?>[] merged = new Class[currentInterfaces.length + interfaces.length];
            System.arraycopy(currentInterfaces, 0, merged, 0, currentInterfaces.length);
            System.arraycopy(interfaces, 0, merged, currentInterfaces.length, interfaces.length);
            // Pre-warm MethodHandle cache for new default methods
            for (Class<?> iface : interfaces) {
                for (Method m : iface.getMethods()) {
                    if (m.isDefault()) {
                        getMethodHandle(m);
                    }
                }
            }
            return merged;
        });

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return (T) Proxy.newProxyInstance(cl,
                classes,
                new MixinProxyHandler<>(target, classes));
    }

    private static List<Class<?>> buildCacheKey(Class<?> originalClass,
                                                 Class<?>[] currentInterfaces,
                                                 Class<?>[] newInterfaces) {
        Class<?>[] key = new Class<?>[1 + currentInterfaces.length + newInterfaces.length];
        key[0] = originalClass;
        System.arraycopy(currentInterfaces, 0, key, 1, currentInterfaces.length);
        System.arraycopy(newInterfaces, 0, key, 1 + currentInterfaces.length, newInterfaces.length);
        return Arrays.asList(key);
    }
}
