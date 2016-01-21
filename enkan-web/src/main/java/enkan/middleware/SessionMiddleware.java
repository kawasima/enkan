package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

/**
 * @author kawasima
 */
@Middleware(name = "session")
public class SessionMiddleware extends AbstractWebMiddleware {
    protected void sessionRequest(HttpRequest request, OptionMap options) {

    }

    protected void sessionResponse(HttpRequest request, OptionMap options) {

    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        sessionRequest(request, OptionMap.of());
        HttpResponse response = castToHttpResponse(next.next(request));
        return response;
    }
}
