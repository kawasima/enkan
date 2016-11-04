package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Headers;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import java.util.Objects;

import static enkan.util.BeanBuilder.builder;

/**
 * CORS setting.
 *
 * @author syobochim
 */
@Middleware(name = "cors")
public class SimpleCORSMiddleware extends AbstractWebMiddleware {
    @Override
    public HttpResponse handle(HttpRequest httpRequest, MiddlewareChain chain) {
        if (isPreflightRequest(httpRequest)) {
            HttpResponse<String> httpResponse = builder(HttpResponse.of("")).set(HttpResponse::setStatus, 200).build();
            httpResponse.setHeaders(Headers.of("Access-Control-Allow-Origin", httpRequest.getHeaders().get("Origin"),
                    "Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS",
                    "Access-Control-Allow-Headers", "Content-Type",
                    "Access-Control-Allow-Credentials", "true"));
            return httpResponse;
        }

        HttpResponse httpResponse = castToHttpResponse(chain.next(httpRequest));
        if (isCORSRequest(httpRequest)) {
            httpResponse.setHeaders(Headers.of("Access-Control-Allow-Origin", httpRequest.getHeaders().get("Origin")));
        }
        return httpResponse;
    }

    private boolean isPreflightRequest(HttpRequest httpRequest) {
        return Objects.equals(httpRequest.getRequestMethod(), "OPTIONS")
                && httpRequest.getHeaders().containsKey("Access-Control-Request-Method");
    }

    private boolean isCORSRequest(HttpRequest httpRequest) {
        return Objects.nonNull(httpRequest.getHeaders().get("Origin"));
    }

}
