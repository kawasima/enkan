package enkan.util;

/**
 *　ThreadingBiFunction is a functional interface that can throw an exception.
 *
 * @param <X> the type of the first argument
 * @param <X1> the type of the second argument
 * @param <Y> the type of the result value
 */
@FunctionalInterface
public interface ThreadingBiFunction<X, X1, Y> {
    Y apply(X x, X1 x1) throws Exception;
}
