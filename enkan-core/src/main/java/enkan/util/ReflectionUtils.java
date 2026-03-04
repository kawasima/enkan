package enkan.util;

import enkan.exception.MisconfigurationException;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class ReflectionUtils {
    /**
     * Returns the classpath entries as a human-readable string.
     *
     * <p>Uses {@code java.class.path} system property so that this works on
     * Java 9+ module-path class loaders, where the application class loader
     * is no longer a {@code URLClassLoader}.
     */
    public static String getClasspathString() {
        String classpath = System.getProperty("java.class.path");
        if (classpath == null || classpath.isBlank()) {
            return "Classpath is empty\n";
        }
        return "  " + Arrays.stream(classpath.split(System.getProperty("path.separator", ":")))
                .collect(Collectors.joining("\n  "))
                + "\n";
    }

    public static <T> T tryReflection(ReflectionRunnable<T> runnable) {
        try {
            return runnable.run();
        } catch (InstantiationException e) {

            throw new MisconfigurationException("core.INSTANTIATION", e.getMessage(), e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            switch (t) {
                case Error error -> throw error;
                case RuntimeException runtimeException -> throw runtimeException;
                case Exception exception -> throw new RuntimeException(t);
                case null, default -> throw new InternalError(t);
            }
        } catch (NoSuchMethodException e) {
            throw new MisconfigurationException("core.NO_SUCH_METHOD", e.getMessage(), e);
        } catch (NoSuchFieldException e) {
            throw new MisconfigurationException("core.NO_SUCH_FIELD", e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new MisconfigurationException("core.ILLEGAL_ACCESS", e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new MisconfigurationException("core.CLASS_NOT_FOUND", e.getMessage(), getClasspathString(), e);
        }
    }
}
