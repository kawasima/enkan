package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

/**
 * The middleware for authentication.
 *
 * @author kawasima
 */
@Middleware(name = "authentication")
public class AuthenticationMiddleware extends AbstractWebMiddleware {
    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        return null;
    }
}
