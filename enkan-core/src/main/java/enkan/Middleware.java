package enkan;

/**
 * Handles a request and calls the next middleware chain and  returns a response.
 *
 * @author kawasima
 */
public interface Middleware<REQ, RES> {
    /**
     * Handles the given request.
     *
     * @param req   A request object
     * @param chain A chain of middlewares
     * @return      A response object
     */
    RES handle(REQ req, MiddlewareChain chain);
}
