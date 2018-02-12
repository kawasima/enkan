package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.OptionMap;
import enkan.data.*;
import enkan.middleware.session.MemoryStore;
import enkan.middleware.session.KeyValueStore;
import enkan.util.MixinUtils;

import javax.validation.constraints.NotNull;

import static enkan.util.ThreadingUtils.some;

/**
 * @author kawasima
 */
@Middleware(name = "session", dependencies = {"cookies"})
public class SessionMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    @NotNull
    private String cookieName;

    private OptionMap cookieAttrs;

    @NotNull
    private KeyValueStore store;

    public SessionMiddleware() {
        store = new MemoryStore();
        cookieName = "enkan-session";
        cookieAttrs = OptionMap.of("httpOnly", true,
                "path", "/");
    }

    protected void populteAttrs(Cookie cookie) {
        if (cookieAttrs.containsValue("domain")) {
            cookie.setDomain(cookieAttrs.getString("domain"));
        }

        if (cookieAttrs.containsValue("path")) {
            cookie.setPath(cookieAttrs.getString("path"));
        }

        if (cookieAttrs.containsValue("secure")) {
            cookie.setSecure(cookieAttrs.getBoolean("secure"));
        }

        if (cookieAttrs.containsValue("httpOnly")) {
            cookie.setHttpOnly(cookieAttrs.getBoolean("httpOnly"));
        }
    }

    protected void sessionRequest(HttpRequest request) {
        some(request.getCookies(), cs -> cs.get(cookieName))
                .ifPresent(sessionCookie -> {
                    String reqKey = sessionCookie.getValue();
                    Session session = reqKey != null ? (Session) store.read(reqKey) : null;
                    request.setSession(session);
                    if (session != null) {
                        ((WebSessionAvailable) request).setSessionKey(reqKey);
                    }
                });
    }

    protected void sessionResponse(HttpResponse response, HttpRequest request) {
        String sessionKey = null;
        if (request instanceof WebSessionAvailable) {
            sessionKey = ((WebSessionAvailable) request).getSessionKey();
        }
        if (response instanceof WebSessionAvailable) {
            Session session = response.getSession();

            // Invalidate session.
            // - Call response.session == null
            // - response.session.isNew && request.session != null
            if (session == null || (session.isNew() && request.getSession() != null)) {
                store.delete(sessionKey);
            }

            String newSessionKey = null;
            if (session != null) {
                if (!PersistentMarkedSession.class.isInstance(session)) {
                    session.persist();
                    newSessionKey = store.write(sessionKey, session);
                }
            }
            Cookie cookie = Cookie.create(cookieName, newSessionKey != null ? newSessionKey : sessionKey);
            populteAttrs(cookie);
            if (newSessionKey != null && !newSessionKey.equals(sessionKey)) {
                response.getCookies().put(cookieName, cookie);
            }
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> chain) {
        request = MixinUtils.mixin(request, WebSessionAvailable.class);
        sessionRequest(request);
        HttpResponse response = castToHttpResponse(chain.next(request));
        response = MixinUtils.mixin(response, WebSessionAvailable.class);
        sessionResponse(response, request);
        return response;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public void setCookieAttrs(OptionMap cookieAttrs) {
        this.cookieAttrs = cookieAttrs;
    }

    public void setStore(KeyValueStore store) {
        this.store = store;
    }
}
