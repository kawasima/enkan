package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.*;

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
                ((FlashAvailable) request).setFlash(flash);
            }
        }
    }

    protected void flashResponse(HttpResponse response, HttpRequest request) {
        if (response == null) return;
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        flashRequest(request);
        HttpResponse response = castToHttpResponse(next.next(request));
        flashResponse(response, request);

        return response;
    }
}
