package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.OptionMap;
import enkan.data.*;
import enkan.middleware.session.MemoryStore;
import enkan.middleware.session.SessionStore;
import enkan.util.MixinUtils;

import javax.validation.constraints.NotNull;

/**
 * @author kawasima
 */
@Middleware(name = "session", dependencies = {"cookies"})
public class SessionMiddleware extends AbstractWebMiddleware {
    private String storeName;
    private String root;

    @NotNull
    private String cookieName;

    private OptionMap cookieAttrs;

    @NotNull
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
            request.setSession(session);
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
            Session session = response.getSession();
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

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public void setCookieAttrs(OptionMap cookieAttrs) {
        this.cookieAttrs = cookieAttrs;
    }

    public void setStore(SessionStore store) {
        this.store = store;
    }
}
