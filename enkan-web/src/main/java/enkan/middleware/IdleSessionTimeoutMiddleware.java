package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Session;
import enkan.util.HttpResponseUtils;

import java.util.Objects;
import java.util.Optional;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.HttpResponseUtils.RedirectStatusCode.TEMPORARY_REDIRECT;
import static enkan.util.ThreadingUtils.some;

/**
 * Expires the idle session after a specified number of seconds.
 *
 * @author kawasima
 */
@Middleware(name = "idleSessionTimeout", dependencies = {"session"})
public class IdleSessionTimeoutMiddleware extends AbstractWebMiddleware<HttpRequest, HttpResponse> {
    private long timeout = 600;
    private Endpoint<HttpRequest, HttpResponse> timeoutEndpoint = req ->
            HttpResponseUtils.redirect("/", TEMPORARY_REDIRECT);
    private static final String SESSION_KEY = IdleSessionTimeoutMiddleware.class.getName() + "/idleTimeout";


    public IdleSessionTimeoutMiddleware() {

    }

    /**
     * Returns a current time seconds from epoch.
     *
     * @return a current time seconds
     */
    private Long currentTime() {
        return System.currentTimeMillis() / 1000;
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain) {
        Optional<Long> endTime = some(request.getSession(),
                session -> session.get(SESSION_KEY),
                obj -> Long.parseLong(Objects.toString(obj)));

        if (endTime.isPresent() && endTime.get() < currentTime()) {
            return builder(timeoutEndpoint.handle(request))
                    .set(HttpResponse::setSession, null)
                    .build();
        } else {
            HttpResponse response = castToHttpResponse(chain.next(request));
            Long nextEndTime = currentTime() + timeout;
            Session session = Optional.ofNullable(response.getSession())
                    .orElse(request.getSession());

            if (session != null) {
                session.put(SESSION_KEY, nextEndTime);
                response.setSession(session);
            }

            return response;
        }
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
