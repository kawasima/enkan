package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

/**
 * @author kawasima
 */
@Middleware(name = "stacktrace")
public class StacktraceMiddleware extends AbstractWebMiddleware {
    protected HttpResponse htmlExResponse(Throwable ex) {
        return null; //TODO
    }

    protected HttpResponse exResponse(HttpRequest request, Throwable ex) {
        String accept = request.getHeaders().get("accept");
        if (accept != null && accept.matches("^text/javascript")) {
            return null;
        } else {
            return htmlExResponse(ex);
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        try {
            return castToHttpResponse(next.next(request));
        } catch (Throwable t) {
            return exResponse(request, t);
        }
    }
}
