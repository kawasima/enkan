package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.data.Cookie;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.AnyPredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.ThreadingUtils.some;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
class CookiesMiddlewareTest {
    private CookiesMiddleware<HttpResponse> middleware;
    private HttpRequest request;

    @BeforeEach
    void setup() {
        middleware = new CookiesMiddleware<>();
        request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders,
                        Headers.of("Host", "example.com"))
                .set(HttpRequest::setScheme, "http")
                .set(HttpRequest::setUri, "/prefix/")
                .set(HttpRequest::setQueryString, "a=b&c=d")
                .build();
    }

    @Test
    void parse() {
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(new AnyPredicate<>(), null,
                (Endpoint<HttpRequest, HttpResponse>) req -> {
                    assertThat(some(req.getCookies().get("A"), Cookie::getValue).orElseThrow(AssertionError::new))
                            .isEqualTo("あいう");
                    assertThat(some(req.getCookies().get("B"), Cookie::getValue).orElseThrow(AssertionError::new))
                            .isEqualTo("1");

                    return builder(HttpResponse.of("hello"))
                            .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                            .build();
                });
        request.getHeaders().put("Cookie", "A=%E3%81%82%E3%81%84%E3%81%86; B=1");
        middleware.handle(request, chain);
    }
}
