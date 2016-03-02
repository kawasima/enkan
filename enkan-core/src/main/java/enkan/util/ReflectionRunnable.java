package enkan.util;

import java.lang.reflect.InvocationTargetException;

/**
 * @author kawasima
 */
public interface ReflectionRunnable<T> {
    T run() throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, NoSuchFieldException, ClassCastException;
}
