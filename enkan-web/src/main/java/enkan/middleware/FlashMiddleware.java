package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.*;
import enkan.util.MixinUtils;

import java.util.Optional;

import static enkan.util.ThreadingUtils.some;

/**
 * Adds session-based flash store.
 *
 * @author kawasima
 */
@Middleware(name = "flash", dependencies = {"session"})
public class FlashMiddleware extends AbstractWebMiddleware {
    private String flashKey = "_flash";

    /**
     * Make the request to handle a flash.
     *
     * @param request request
     */
    protected void flashRequest(HttpRequest request) {
        Session session = request.getSession();
        if (session != null && session.containsKey(flashKey)) {
            request.setFlash((Flash) session.remove(flashKey));
        }
    }

    /**
     * Make the response to handle a flash.
     *
     * @param response response
     * @param request  request
     */
    protected void flashResponse(HttpResponse response, HttpRequest request) {
        if (response == null) return;

        Session session = response.getSession();
        if (session == null || PersistentMarkedSession.class.isInstance(session)) {
            session = request.getSession();
        }

        Flash responseFlash = FlashAvailable.class.cast(response).getFlash();
        if (responseFlash != null) {
            if (session == null) {
                session = new Session();
            }
            session.put(flashKey, responseFlash);
        }

        if (session != null) {
            response.setSession(session);
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        request = MixinUtils.mixin(request, FlashAvailable.class);
        flashRequest(request);

        HttpResponse response = castToHttpResponse(next.next(request));

        response = MixinUtils.mixin(response, FlashAvailable.class);
        flashResponse(response, request);

        return response;
    }

    /**
     * Sets the key of flash in a session.
     *
     * @param flashKey the key of flash in a session
     */
    public void setFlashKey(String flashKey) {
        this.flashKey = flashKey;
    }
}
