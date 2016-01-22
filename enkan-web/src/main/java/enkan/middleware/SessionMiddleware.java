package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.SessionAvailable;
import enkan.util.MixinUtils;

/**
 * @author kawasima
 */
@Middleware(name = "session")
public class SessionMiddleware extends AbstractWebMiddleware {
    private String storeName;
    private String root;
    private String cookieName;
    private OptionMap cookieAttrs;

    public SessionMiddleware() {

    }
    protected void sessionRequest(HttpRequest request) {

    }

    protected void bareSessionResponse(HttpResponse response, HttpRequest request) {

    }
    protected void sessionResponse(HttpResponse response, HttpRequest request) {

    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        sessionRequest(request);
        HttpResponse response = castToHttpResponse(next.next(request));
        response = MixinUtils.mixin(response, SessionAvailable.class);
        sessionResponse(response, request);
        return response;
    }
}
