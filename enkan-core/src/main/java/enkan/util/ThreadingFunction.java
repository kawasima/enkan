package enkan.util;

/**
 * @author kawasima
 */
public interface ThreadingFunction<T, R> {
    R apply(T t) throws Exception;
}
