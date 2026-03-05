package enkan.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashSet;
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

    private static MethodHandle lookupSpecial(Method m) {
        Class<?> declaringClass = m.getDeclaringClass();
        return tryReflection(() -> {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup());
            return lookup.unreflectSpecial(m, declaringClass);
        });
    }

    private static MethodHandle lookupDelegate(Method m) {
        return tryReflection(() -> {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                    m.getDeclaringClass(), MethodHandles.lookup());
            return lookup.unreflect(m);
        });
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
                        MethodHandle mh = getDelegateHandle(method);
                        if (args == null || args.length == 0) {
                            return mh.invoke(original);
                        } else {
                            Object[] fullArgs = new Object[args.length + 1];
                            fullArgs[0] = original;
                            System.arraycopy(args, 0, fullArgs, 1, args.length);
                            return mh.invokeWithArguments(fullArgs);
                        }
                    }
                } else {
                    return getMethodHandle(method)
                            .bindTo(proxy)
                            .invokeWithArguments(args);
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

        final LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<>();
        getAllInterfaces(clazz, interfacesFound);

        return interfacesFound.toArray(new Class<?>[0]);
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

        // When request is
        final Class<?> targetClass = target.getClass();
        if (Arrays.stream(interfaces).allMatch(i -> i.isAssignableFrom(targetClass))) {
            return target;
        }

        Class<?>[] classes;
        int addedIndex;
        if (Proxy.isProxyClass(targetClass)) {
            MixinProxyHandler<T> handler = ((MixinProxyHandler<T>) Proxy.getInvocationHandler(target));
            target = handler.original();
            Class<?>[] originalInterfaces = handler.proxyInterfaces();
            classes = new Class[originalInterfaces.length + interfaces.length];
            System.arraycopy(originalInterfaces, 0, classes, 0, originalInterfaces.length);
            addedIndex = originalInterfaces.length;
        } else {
            Class<?>[] targetInterfaces = getAllInterfaces(targetClass);
            classes = new Class[targetInterfaces.length + interfaces.length];
            System.arraycopy(targetInterfaces, 0, classes, 0, targetInterfaces.length);
            addedIndex = targetInterfaces.length;
        }
        Arrays.asList(interfaces).forEach(i ->
                Arrays.stream(i.getMethods())
                        .filter(Method::isDefault)
                        .forEach(MixinUtils::getMethodHandle));


        System.arraycopy(interfaces, 0, classes, addedIndex, interfaces.length);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return (T) Proxy.newProxyInstance(cl,
                classes,
                new MixinProxyHandler<>(target, classes));

    }
}
