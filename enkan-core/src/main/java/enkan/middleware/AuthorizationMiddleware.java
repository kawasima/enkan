package enkan.middleware;

import enkan.Endpoint;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.exception.AuthorizationException;

/**
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "authorization")
public class AuthorizationMiddleware<REQ, RES> implements Middleware<REQ, RES> {
    private Endpoint<REQ, RES> authorizationExceptionHandler;
    @Override
    public RES handle(REQ req, MiddlewareChain next) {
        try {
            return (RES) next.next(req);
        } catch (AuthorizationException ex) {
            if (authorizationExceptionHandler != null) {
                return authorizationExceptionHandler.handle(req);
            } else {
                throw ex;
            }
        }
    }
}
