package enkan.middleware.throttling;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.AbstractWebMiddleware;

/**
 * @author kawasima
 */
@Middleware(name = "throttling")
public class ThrottlingMiddleware extends AbstractWebMiddleware {
    @Override
    public HttpResponse handle(HttpRequest httpRequest, MiddlewareChain chain) {
        return null;
    }
}
