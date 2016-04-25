package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Session;
import enkan.util.HttpResponseUtils;

import java.util.Optional;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.HttpResponseUtils.RedirectStatusCode.TEMPORARY_REDIRECT;

/**
 * Expires the idle session after a specified number of seconds.
 *
 * @author kawasima
 */
public class IdleSessionTimeoutMiddleware extends AbstractWebMiddleware {
    private long timeout = 600;
    private Endpoint<HttpRequest, HttpResponse> timeoutEndpoint = req ->
            HttpResponseUtils.redirect("/", TEMPORARY_REDIRECT);
    private static final String SESSION_KEY = IdleSessionTimeoutMiddleware.class.getName() + "/idleTimeout";


    public IdleSessionTimeoutMiddleware() {

    }

    private Long currentTime() {
        return System.currentTimeMillis() / 1000;
    }
    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
        Session session = Optional.ofNullable(request.getSession())
                .orElse(new Session());
        Long endTime = session.getAttribute(SESSION_KEY);
        if (endTime != null && endTime < currentTime()) {
            return builder(timeoutEndpoint.handle(request))
                    .set(HttpResponse::setSession, null)
                    .build();
        } else {
            HttpResponse response = castToHttpResponse(chain.next(request));
            endTime = currentTime() + timeout;
            Session responseSession = Optional.ofNullable(response.getSession())
                    .orElse(session);
            session.setAttribute(SESSION_KEY, endTime);
            response.setSession(responseSession);

            return response;
        }
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
