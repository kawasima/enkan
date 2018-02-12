package enkan.middleware.throttling;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.AbstractWebMiddleware;
import enkan.throttling.Throttle;
import enkan.util.HttpResponseUtils;

import java.util.Collections;
import java.util.List;

import static enkan.util.BeanBuilder.builder;

/**
 * @author kawasima
 */
@Middleware(name = "throttling")
public class ThrottlingMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    private List<Throttle> throttles = Collections.emptyList();

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> chain) {
        if (throttles.stream()
                .anyMatch(throttle -> throttle.apply(request))) {
            return builder(HttpResponseUtils.response("Too Many Request"))
                    .set(HttpResponse::setStatus, 429)
                    .build();
        }
        return castToHttpResponse(chain.next(request));
    }

    public void setThrottles(List<Throttle> throttles) {
        this.throttles = throttles;
    }
}
