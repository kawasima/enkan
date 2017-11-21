package enkan;

import enkan.exception.MisconfigurationException;
import enkan.predicate.AnyPredicate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The application applied middlewares.
 *
 * When you use a middleware, call the <code>use</code> method.
 *
 * @author kawasima
 */
public interface Application<REQ, RES> {
    AnyPredicate ANY = new AnyPredicate<>();

    /**
     * Declare to use a middleware.
     *
     * @param middleware middleware
     * @param <IN>  A type of request
     * @param <OUT> A type of response
     */
    default <IN, OUT> void use(Middleware<IN, OUT> middleware) {
        use(ANY, middleware);
    }

    /**
     * Declare to use a middleware with a predication.
     *
     * @param predicate A predicate for using the middleware
     * @param middleware A middleware
     * @param <IN>
     * @param <OUT>
     */
    default <IN, OUT> void use(Predicate<IN> predicate, Middleware<IN, OUT> middleware) {
        use(predicate, null, middleware);
    }

    /**
     * Declare to use a middleware with a predication and middleware's name.
     *
     * @param predicate
     * @param middlewareName
     * @param middleware
     * @param <IN>
     * @param <OUT>
     */
    <IN, OUT> void use(Predicate<IN> predicate, String middlewareName, Middleware<IN, OUT> middleware);

    /**
     * Handle a request using middleware stack in this application.
     *
     * @param req   A request object
     * @return      A response object
     */
    RES handle(REQ req);

    /**
     * Validate a middleware stack.
     */
    default void validate() {
        Set<String> priorMiddelwares = new HashSet<>();
        priorMiddelwares.add("");

        for (MiddlewareChain chain : getMiddlewareStack()) {
            Middleware middleware = chain.getMiddleware();
            enkan.annotation.Middleware anno = middleware.getClass().getAnnotation(enkan.annotation.Middleware.class);
            if (anno == null) continue;
            String name = anno.name();
            String[] dependencies = anno.dependencies();
            Set<String> lackMiddlewares = Arrays.stream(dependencies)
                    .filter(d -> !priorMiddelwares.contains(d))
                    .collect(Collectors.toSet());

            if (!lackMiddlewares.isEmpty()) {
                throw new MisconfigurationException("core.MIDDLEWARE_DEPENDENCY", name, lackMiddlewares);
            }
            priorMiddelwares.add(name);
        }
    }

    /**
     * Get all middlewares using by this application.
     *
     * @return List of middlewares
     */
    List<MiddlewareChain<?, ?>> getMiddlewareStack();
}
