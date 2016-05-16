package enkan;

/**
 * Endpoint is a specialized middleware.
 * It doesn't have a next middleware.
 *
 * @author kawasima
 */
public interface Endpoint<REQ, RES> extends Middleware<REQ, RES> {
    /**
     *
     * @param req   {@inheritDoc}
     * @param next  {@inheritDoc}
     * @return      {@inheritDoc}
     */
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
