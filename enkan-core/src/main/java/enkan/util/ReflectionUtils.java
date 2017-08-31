package enkan.util;

import enkan.exception.MisconfigurationException;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kawasima
 */
public class ReflectionUtils {
    private static String getClasspathString() {
        ClassLoader cl = Optional.ofNullable(Thread.currentThread().getContextClassLoader())
                .orElse(ClassLoader.getSystemClassLoader());

        while (!URLClassLoader.class.isInstance(cl)) {
            cl = cl.getParent();
            if (cl == null) return "Classpath is empty";
        }

        return "  " + Stream.of(cl).map(URLClassLoader.class::cast)
                .flatMap(c -> Arrays.stream(c.getURLs()))
                .map(URL::getFile)
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
            if (t instanceof Error) {
                throw (Error) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Exception) {
                throw new RuntimeException(t);
            } else {
                throw new InternalError(t);
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
