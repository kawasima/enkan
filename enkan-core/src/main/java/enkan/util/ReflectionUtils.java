package enkan.util;

import enkan.exception.MisconfigurationException;

import java.lang.reflect.InvocationTargetException;

/**
 * @author kawasima
 */
public class ReflectionUtils {
    public static <T> T tryReflection(ReflectionRunnable<T> runnable) {
        try {
            return runnable.run();
        } catch (InstantiationException e) {
            throw MisconfigurationException.raise("");
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof Error) {
                throw (Error) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Exception) {
                throw MisconfigurationException.raise("");
            } else {
                throw new InternalError(t);
            }
        } catch (NoSuchMethodException e) {
            throw MisconfigurationException.raise("NO_SUCH_METHOD");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
