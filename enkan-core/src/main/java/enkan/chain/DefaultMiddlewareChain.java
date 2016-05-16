package enkan.chain;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.data.Traceable;

import java.util.function.Predicate;

/**
 * The default chain of middleware.
 *
 * @author kawasima
 */
public class DefaultMiddlewareChain<REQ, RES> implements MiddlewareChain<REQ, RES> {
    private Predicate<REQ> predicate;
    private Middleware<REQ, RES> middleware;
    private String middlewareName;
    private MiddlewareChain<Object, Object> next;


    /**
     * Creates the chain of middleware.
     *
     * If middlewareName is not given, the name of middleware is given the default from the annotation.
     *
     * @param predicate       a condition of applying the middleware
     * @param middlewareName  a name of middleware
     * @param middleware      a middleware
     */
    public DefaultMiddlewareChain(Predicate<REQ> predicate, String middlewareName, Middleware<REQ, RES> middleware) {
        this.predicate = predicate;
        this.middleware = middleware;
        enkan.annotation.Middleware anno = middleware.getClass().getAnnotation(enkan.annotation.Middleware.class);
        if (middlewareName != null) {
            this.middlewareName = middlewareName;
        } else if (anno != null) {
            this.middlewareName = anno.name();
        } else {
            this.middlewareName = "Anonymous(" + middleware.toString() + ")";
        }
    }

    /**
     * Sets the next chain of middleware.
     *
     * @param next {@inheritDoc}
     * @return     {@inheritDoc}
     */
    @Override
    public MiddlewareChain<REQ, RES> setNext(MiddlewareChain next) {
        this.next = next;
        return this;
    }

    /**
     * Gets middleware.
     *
     * @return {@inheritDoc}
     */
    @Override
    public Middleware<REQ, RES> getMiddleware() {
        return middleware;
    }

    protected void writeTraceLog(Object reqOrRes, String middlewareName) {
        if (reqOrRes instanceof Traceable) {
            ((Traceable) reqOrRes).getTraceLog().write(middlewareName);
        }
    }

    /**
     * Dispatches a request to the next chain of middleware.
     *
     * @param req  {@inheritDoc}
     * @return     {@inheritDoc}
     */
    @Override
    public RES next(REQ req) {
        writeTraceLog(req, middlewareName);

        if (predicate.test(req)) {
            RES res = middleware.handle(req, next);
            writeTraceLog(res, middlewareName);
            return res;
        } else if (next != null){
            RES res = (RES) next.next(req);
            writeTraceLog(res, middlewareName);
            return res;
        } else {
            return null;
        }
    }

    /**
     * Gets the name of middleware.
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return middlewareName;
    }

    /**
     * Gets the predicate.
     *
     * @return {@inheritDoc}
     */
    @Override
    public Predicate<REQ> getPredicate() {
        return predicate;
    }

    /**
     * Sets the predicate.
     *
     * If this predicate returns true, middleware will be applied.
     *
     * @param predicate predicate for applying the middleware.
     */
    @Override
    public void setPredicate(Predicate<REQ> predicate) {
        this.predicate = predicate;
    }

    @Override
    public String toString() {
        return predicate.toString() + "   " + middlewareName
                + " (" + middleware + ")";
    }
}
