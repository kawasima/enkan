package enkan;

/**
 * A middleware.
 *
 * @author kawasima
 */
public interface Middleware<REQ, RES> {
    /**
     * Handle a request.
     *
     * @param req   A request object
     * @param chain A chain of middlewares
     * @return      A response object
     */
    RES handle(REQ req, MiddlewareChain chain);
}
