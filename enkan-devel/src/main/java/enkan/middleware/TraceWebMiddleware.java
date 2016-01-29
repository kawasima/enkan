package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

/**
 * @author kawasima
 */
@Middleware(name = "traceWeb")
public class TraceWebMiddleware extends AbstractWebMiddleware {
    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        return castToHttpResponse(next.next(request));
    }
}
