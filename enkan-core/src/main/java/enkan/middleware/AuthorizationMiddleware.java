package enkan.middleware;

import enkan.Endpoint;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.exception.AuthorizationException;

/**
 * The middleware for authorization.
 *
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "authorization")
public class AuthorizationMiddleware<REQ, RES> implements Middleware<REQ, RES> {
    private Endpoint<REQ, RES> authorizationExceptionHandler;

    /**
     * The handler when exception occurs during authorization.
     *
     * @param req   A request object
     * @param chain next middlewares
     * @return      A response object
     */
    @Override
    public RES handle(REQ req, MiddlewareChain chain) {
        try {
            return (RES) chain.next(req);
        } catch (AuthorizationException ex) {
            if (authorizationExceptionHandler != null) {
                return authorizationExceptionHandler.handle(req);
            } else {
                throw ex;
            }
        }
    }
}
