package enkan.util;

import enkan.exception.FalteringEnvironmentException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;

/**
 * @author kawasima
 */
public class ThreadingUtils {
    private static final Set<Class<? extends Exception>> DEFAULT_ILLEGAL_ARGUMENT_EXCEPTIONS =
            new HashSet<Class<? extends Exception>>() {{
                add(URISyntaxException.class);
                add(MalformedURLException.class);
                add(UnsupportedEncodingException.class);
                add(UnsupportedCharsetException.class);
            }};

    private static <X, Y> Optional<Y> doSome(X start, ThreadingFunction... functions) {
        if (functions == null || start == null) {
            return Optional.ofNullable((Y) start);
        }

        Object v = start;
        LinkedList<ThreadingFunction> funcQueue = new LinkedList<>(Arrays.asList(functions));
        while(!funcQueue.isEmpty()) {
            ThreadingFunction f = funcQueue.removeFirst();
            try {
                v = f.apply(v);
            } catch (Exception e) {
                if (DEFAULT_ILLEGAL_ARGUMENT_EXCEPTIONS.contains(e.getClass())) {
                    throw new IllegalArgumentException(e);
                } else {
                    throw FalteringEnvironmentException.create(e);
                }
            }
            if (v == null) {
                return Optional.empty();
            }
        }
        return Optional.of((Y) v);
    }

    public static <X, Y> Optional<Y> some(X start, ThreadingFunction<X, Y> f1) {
        return doSome(start, new ThreadingFunction[]{ f1 });
    }

    public static <X0, X1, Y> Optional<Y> some(X0 start, ThreadingFunction<X0, X1> f1, ThreadingFunction<X1, Y> f2) {
        return doSome(start, new ThreadingFunction[]{ f1, f2 });
    }

    public static <X0, X1, X2, Y> Optional<Y> some(X0 start,
                                         ThreadingFunction<X0, X1> f1,
                                         ThreadingFunction<X1, X2> f2,
                                         ThreadingFunction<X2, Y> f3) {
        return doSome(start, new ThreadingFunction[]{ f1, f2, f3 });
    }

    public static <X0, X1, X2, X3, Y> Optional<Y> some(
            X0 start,
            ThreadingFunction<X0, X1> f1,
            ThreadingFunction<X1, X2> f2,
            ThreadingFunction<X2, X3> f3,
            ThreadingFunction<X2, Y> f4) {
        return doSome(start, new ThreadingFunction[]{ f1, f2, f3, f4});
    }

    public static <X, X1, Y> ThreadingFunction<X, Y> partial(ThreadingBiFunction<X, X1, Y> f, X1 arg) {
        return x -> f.apply(x, arg);
    }

    interface ThreadingBiFunction<X, X1, Y> {
        Y apply(X x, X1 x1) throws Exception;
    }
}
