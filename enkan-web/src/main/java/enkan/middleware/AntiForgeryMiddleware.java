package enkan.middleware;


import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Headers;
import enkan.data.ForgeryDetectable;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Session;
import enkan.util.MixinUtils;
import enkan.util.ThreadingUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static enkan.util.BeanBuilder.builder;

/**
 * Sets the token string to the session object for the anti forgery.
 *
 * @author kawasima
 */
@Middleware(name = "antiForgery", dependencies = {"session"})
public class AntiForgeryMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    private static final String TOKEN_KEY = AntiForgeryMiddleware.class.getName()
            + "/antiForgeryToken";

    private String newToken() {
        return UUID.randomUUID().toString();
    }

    protected Optional<String> sessionToken(HttpRequest request) {
        return ThreadingUtils.some(request,
                HttpRequest::getSession,
                s -> s.get(TOKEN_KEY),
                Objects::toString);
    }

    /**
     * Puts the token to the session.
     *
     * @param response a HttpResponse object
     * @param request a HttpRequest object
     * @param token a String contains the new token
     */
    protected void putSessionToken(HttpResponse response, HttpRequest request, String token) {
        String oldToken = sessionToken(request).orElse(null);
        if (!Objects.equals(token, oldToken)) {
            Session session = Optional.ofNullable(request.getSession())
                    .orElse(new Session());
            session.put(TOKEN_KEY, token);
            response.setSession(session);
        }
    }

    private Map<String, ?> formParams(HttpRequest request) {
        return request.getParams();
    }

    private Optional<String> defaultRequestToken(HttpRequest request) {
        return ThreadingUtils.some(request, this::formParams, p -> p.get("__anti-forgery-token"), Object::toString);
    }

    private boolean isValidRequest(HttpRequest request) {
        Optional<String> readToken = defaultRequestToken(request);
        Optional<String> storedToken = sessionToken(request);
        return readToken.isPresent() && storedToken.isPresent() && readToken.get().equals(storedToken.get());
    }

    private boolean isGetRequest(HttpRequest request) {
        String method = request.getRequestMethod();
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method);
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> next) {
        String token = sessionToken(request).orElseGet(this::newToken);
        if (!isGetRequest(request) && !isValidRequest(request)) {
            return builder(HttpResponse.of("<h1>Invalid anti-forgery token</h1>"))
                    .set(HttpResponse::setStatus, 403)
                    .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                    .build();
        } else {
            request = MixinUtils.mixin(request, ForgeryDetectable.class);
            ForgeryDetectable.class.cast(request).setAntiForgeryToken(token);
            HttpResponse response = castToHttpResponse(next.next(request));
            putSessionToken(response, request, token);
            return response;
        }
    }
}
