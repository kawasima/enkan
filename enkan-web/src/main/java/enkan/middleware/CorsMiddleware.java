package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Headers;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.HttpResponseUtils.header;
import static enkan.util.ThreadingUtils.some;

/**
 * CORS setting.
 *
 * @author syobochim
 */
@Middleware(name = "cors")
public class CorsMiddleware extends AbstractWebMiddleware {
    private Set<String> methods;
    private Set<String> origins;
    private Set<String> headers;
    private Long maxage;
    private boolean credentials;

    public CorsMiddleware() {
        methods = new HashSet<>(Arrays.asList("GET", "POST", "DELETE", "PUT", "HEAD", "OPTIONS"));
        origins = new HashSet<>(Arrays.asList("*"));
        headers = new HashSet<>(Arrays.asList(
                "Origin", "Accept", "X-Requested-With", "Content-Type",
                "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        maxage = 1800L;
        credentials = true;
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
        if (isCORSRequest(request)) {
            if (!isOriginAllowed(request) || !methods.stream().anyMatch(x -> x.equalsIgnoreCase(request.getRequestMethod()))) {
                return invalidCors(request);
            }
            if (isPreflightRequest(request)) {
                Headers responseHeaders = Headers.empty();
                if (isAnyOriginAllowed()) {
                    responseHeaders.put("Access-Control-Allow-Origin", "*");
                } else {
                    String origin = some(request.getHeaders(),
                            headers -> headers.get("origin"))
                            .orElse("*");
                    responseHeaders.put("Access-Control-Allow-Origin", origin);
                }

                if (origins != null && !origins.isEmpty()) {
                }
                if (methods != null && !methods.isEmpty()) {
                    responseHeaders.put("Access-Control-Allow-Methods", String.join(", ", methods));
                }
                if (headers != null && !headers.isEmpty()) {
                    responseHeaders.put("Access-Control-Allow-Headers", String.join(", ", headers));
                }
                if (credentials) {
                    responseHeaders.put("Access-Control-Allow-Credentials", "true");
                }
                if (maxage > 0L) {
                    responseHeaders.put("Access-Control-Max-Age", String.valueOf(maxage));
                }
                return builder(HttpResponse.of(""))
                        .set(HttpResponse::setStatus, 200)
                        .set(HttpResponse::setHeaders, responseHeaders)
                        .build();
            }
        }

        HttpResponse response = castToHttpResponse(chain.next(request));

        if (isCORSRequest(request)) {
            if (origins != null && !origins.isEmpty()) {
                header(response, "Access-Control-Allow-Origin", String.join(", ", origins));
            }
        }
        return response;
    }

    private HttpResponse invalidCors(HttpRequest request) {
        return builder(HttpResponse.of("Invalid CORS request; Origin="
                + request.getHeaders().get("origin")
                + ", Method="
                + request.getRequestMethod()))
                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/plain"))
                .set(HttpResponse::setStatus, 403)
                .build();
    }

    private boolean isOriginAllowed(HttpRequest request) {
        return some(request.getHeaders(),
                headers -> headers.get("origin"),
                origin -> isAnyOriginAllowed() || origins.contains(origin))
                .orElse(false);
    }

    /**
     * Determines if any origin is allowed.
     *
     * @return true if any origin is allowed
     */
    private boolean isAnyOriginAllowed() {
        return origins.contains("*");
    }

    private boolean isPreflightRequest(HttpRequest httpRequest) {
        return Objects.equals(httpRequest.getRequestMethod().toUpperCase(), "OPTIONS")
                && httpRequest.getHeaders().containsKey("Access-Control-Request-Method");
    }

    private boolean isCORSRequest(HttpRequest httpRequest) {
        return Objects.nonNull(httpRequest.getHeaders().get("Origin"));
    }

    /**
     * Set the allowed methods.
     *
     * @param methods A set of allowed methods
     */
    public void setMethods(Set<String> methods) {
        this.methods = methods;
    }

    /**
     * Set the allowed origins.
     *
     * @param origins A set of allowed origins
     */
    public void setOrigins(Set<String> origins) {
        this.origins = origins;
    }

    /**
     * Set the allowed headers.
     *
     * @param headers A set of allowed headers
     */
    public void setHeaders(Set<String> headers) {
        this.headers = headers;
    }

    /**
     * Set the max age.
     *
     * @param maxage max age
     */
    public void setMaxage(Long maxage) {
        this.maxage = maxage;
    }

    /**
     * Set the credentials.
     *
     * @param credentials credentials
     */
    public void setCredentials(boolean credentials) {
        this.credentials = credentials;
    }
}
