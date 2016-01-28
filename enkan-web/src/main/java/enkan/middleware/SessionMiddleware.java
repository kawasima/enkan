package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.OptionMap;
import enkan.data.*;
import enkan.middleware.session.MemoryStore;
import enkan.middleware.session.SessionStore;
import enkan.util.MixinUtils;

import javax.swing.text.html.Option;

/**
 * @author kawasima
 */
@Middleware(name = "session", dependencies = {"cookies"})
public class SessionMiddleware extends AbstractWebMiddleware {
    private String storeName;
    private String root;
    private String cookieName;
    private OptionMap cookieAttrs;
    private SessionStore store;

    public SessionMiddleware() {
        store = new MemoryStore();
        cookieName = "enkan-session";
        cookieAttrs = OptionMap.of("httpOnly", true,
                "path", "/");
    }

    protected void sessionRequest(HttpRequest request) {
        if (request instanceof WebSessionAvailable) {
            Cookie sessionCookie = request.getCookies().get(cookieName);

            String reqKey = sessionCookie != null ? sessionCookie.getValue() : null;
            Session session = reqKey != null ? store.read(reqKey) : null;
            ((WebSessionAvailable) request).setSession(session);
            if (session != null) {
                ((WebSessionAvailable) request).setSessionKey(reqKey);
            }
        }
    }

    protected void sessionResponse(HttpResponse response, HttpRequest request) {
        String sessionKey = null;
        if (request instanceof WebSessionAvailable) {
            sessionKey = ((WebSessionAvailable) request).getSessionKey();
        }
        if (response instanceof WebSessionAvailable) {
            Session session = ((WebSessionAvailable) response).getSession();
            String newSessionKey = null;
            if (session != null) {
                if (session.isValid()) {
                    newSessionKey = store.write(sessionKey, session);
                } else {
                    if (sessionKey != null) {
                        newSessionKey = store.delete(sessionKey);
                    }
                }
            }
            Cookie cookie = Cookie.create(cookieName, newSessionKey != null ? newSessionKey : sessionKey);

            if (newSessionKey != null && !newSessionKey.equals(sessionKey)) {
                response.getCookies().put(cookieName, cookie);
            }
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        request = MixinUtils.mixin(request, WebSessionAvailable.class);
        sessionRequest(request);
        HttpResponse response = castToHttpResponse(next.next(request));
        response = MixinUtils.mixin(response, WebSessionAvailable.class);
        sessionResponse(response, request);
        return response;
    }
}
