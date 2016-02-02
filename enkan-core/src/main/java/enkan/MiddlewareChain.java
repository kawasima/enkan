package enkan;

import java.util.function.Predicate;

/**
 * A chain of middlewares.
 *
 * @author kawasima
 */
public interface MiddlewareChain<REQ, RES> {
    /**
     * Set a next middleware.
     *
     * @param next   a next middleware
     * @return this middleware's chain
     */
    MiddlewareChain<REQ, RES> setNext(MiddlewareChain next);

    /**
     * Get a middleware in this chain.
     *
     * @return middleware
     */
    Middleware<REQ, RES> getMiddleware();

    /**
     * Get a middleware name in this chain.
     *
     * @return the name of middleware
     */
    String getName();

    /**
     * Get a predicate.
     *
     * @return
     */
    Predicate<REQ> getPredicate();

    /**
     * Set a predicate.
     *
     * @param predicate predicate
     */
    void setPredicate(Predicate<REQ> predicate);

    /**
     * Process the next middleware.
     *
     * @param req  A request object
     * @return A response object
     */
    RES next(REQ req);
}
