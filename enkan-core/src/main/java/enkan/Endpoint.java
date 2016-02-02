package enkan;

/**
 * Endpoint is a middleware doesn't have next middleware.
 *
 * @author kawasima
 */
public interface Endpoint<REQ, RES> extends Middleware<REQ, RES> {
    default RES handle(REQ req, MiddlewareChain next) {
        return handle(req);
    }

    /**
     * Handle a request.
     *
     * @param req  A request object
     * @return     A response object
     */
    RES handle(REQ req);
}
