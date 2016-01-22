package enkan.util;

import enkan.exception.UnrecoverableException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kawasima
 */
public class MixinUtils {
    private static ConcurrentHashMap<Method, MethodHandle> methodHandleCache = new ConcurrentHashMap<>();

    private static MethodHandle lookupSpecial(Method method) {
        final Class<?> declaringClass = method.getDeclaringClass();
        MethodHandles.Lookup lookup = MethodHandles.publicLookup()
                .in(declaringClass);
        try {
            final Field f = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
            final int modifiers = f.getModifiers();
            if (Modifier.isFinal(modifiers)) {
                final Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, modifiers & ~Modifier.FINAL);
                f.setAccessible(true);
                f.set(lookup, MethodHandles.Lookup.PRIVATE);
            }
            return lookup.unreflectSpecial(method, declaringClass);
        } catch (Exception e) {
            throw UnrecoverableException.create(e);
        }
    }

    static MethodHandle getMethodHandle(Method method) {
        return  methodHandleCache.computeIfAbsent(method, MixinUtils::lookupSpecial);
    }

    static class MixinProxyHandler<T> implements InvocationHandler {
        private T original;
        private Class[] proxyInterfaces;
        MixinProxyHandler(T original, Class[] proxyInterfaces) {
            this.original = original;
            this.proxyInterfaces = proxyInterfaces;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass().isAssignableFrom(original.getClass())) {
                return method.invoke(original, args);
            } else {
                return getMethodHandle(method)
                        .bindTo(proxy)
                        .invokeWithArguments(args);
            }
        }

        public T getOriginal() {
            return original;
        }

        public Class[] getProxyInterfaces() {
            return proxyInterfaces;
        }
    }

    public static <T> T mixin(T target, Class<?>... interfaces) {
        if (target == null) return null;

        // When request is
        final Class<?> targetClass = target.getClass();
        if (Arrays.stream(interfaces).allMatch(i -> i.isAssignableFrom(targetClass))) {
            return target;
        }

        Class[] classes;
        int addedIndex = 0;
        if (Proxy.isProxyClass(targetClass)) {
            MixinProxyHandler<T> handler = ((MixinProxyHandler<T>) Proxy.getInvocationHandler(target));
            target = handler.getOriginal();
            Class[] originalInterfaces = handler.getProxyInterfaces();
            classes = new Class[originalInterfaces.length + interfaces.length];
            System.arraycopy(originalInterfaces, 0, classes, 0, originalInterfaces.length);
            addedIndex = originalInterfaces.length;
        } else {
            Class[] targetInterfaces = targetClass.getInterfaces();
            classes = new Class[targetInterfaces.length + interfaces.length];
            System.arraycopy(targetInterfaces, 0, classes, 0, targetInterfaces.length);
            addedIndex = targetInterfaces.length;
        }
        Arrays.asList(interfaces).forEach(i ->
                Arrays.asList(i.getMethods()).stream()
                        .filter(Method::isDefault)
                        .forEach(m -> getMethodHandle(m)));


        System.arraycopy(interfaces, 0, classes, addedIndex, interfaces.length);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return (T) Proxy.newProxyInstance(cl,
                classes,
                new MixinProxyHandler(target, classes));

    }
}
