package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.Flash;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Session;

/**
 *
 * TODO implemtenting...
 *
 * @author kawasima
 */
@Middleware(name = "flash", dependencies = {"session"})
public class FlashMiddleware extends AbstractWebMiddleware {
    protected void flashRequest(HttpRequest request) {
        Session session = request.getSession();
        if (session != null) {
            Flash flash = session.getAttribute("_flash");
            session.removeAttribute("_flash");
            if (flash != null) {

            }
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        flashRequest(request);
        HttpResponse response = castToHttpResponse(next.next(request));
        return response;
    }
}
