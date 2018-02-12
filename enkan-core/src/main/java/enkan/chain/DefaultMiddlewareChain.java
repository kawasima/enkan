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
public class DefaultMiddlewareChain<REQ, RES, NREQ, NRES> implements MiddlewareChain<REQ, RES, NREQ, NRES> {
    private Predicate<? super REQ> predicate;
    private Middleware<REQ, RES, NREQ, NRES> middleware;
    private String middlewareName;
    private MiddlewareChain<NREQ, NRES, ?, ?> chain;


    /**
     * Creates the chain of middleware.
     *
     * If middlewareName is not given, the name of middleware is given the default from the annotation.
     *
     * @param predicate       a condition of applying the middleware
     * @param middlewareName  a name of middleware
     * @param middleware      a middleware
     */
    public DefaultMiddlewareChain(Predicate<? super REQ> predicate, String middlewareName, Middleware<REQ, RES, NREQ, NRES> middleware) {
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
     * Sets the chain chain of middleware.
     *
     * @param next {@inheritDoc}
     * @return     {@inheritDoc}
     */
    @Override
    public MiddlewareChain<REQ, RES, NREQ, NRES> setNext(MiddlewareChain<NREQ, NRES, ?, ?> next) {
        this.chain = next;
        return this;
    }

    /**
     * Gets middleware.
     *
     * @return {@inheritDoc}
     */
    @Override
    public Middleware<REQ, RES, NREQ, NRES> getMiddleware() {
        return middleware;
    }

    protected void writeTraceLog(Object reqOrRes, String middlewareName) {
        if (reqOrRes instanceof Traceable) {
            ((Traceable) reqOrRes).getTraceLog().write(middlewareName);
        }
    }

    /**
     * Dispatches a request to the chain chain of middleware.
     *
     * @param req  {@inheritDoc}
     * @return     {@inheritDoc}
     */
    @Override
    public RES next(REQ req) {
        writeTraceLog(req, middlewareName);

        if (predicate.test(req)) {
            RES res = middleware.handle(req, chain);
            writeTraceLog(res, middlewareName);
            return res;
        } else if (chain != null){
            RES res = (RES) chain.next((NREQ) req);
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
    public Predicate<? super REQ> getPredicate() {
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
    public void setPredicate(Predicate<? super REQ> predicate) {
        this.predicate = predicate;
    }

    @Override
    public String toString() {
        return predicate.toString() + "   " + middlewareName
                + " (" + middleware + ")";
    }
}
