package enkan.util;

/**
 * ThreadingFunction is a functional interface that can throw an exception.
 *
 * @author kawasima
 */
@FunctionalInterface
public interface ThreadingFunction<T, R> {
    R apply(T t) throws Exception;
}
