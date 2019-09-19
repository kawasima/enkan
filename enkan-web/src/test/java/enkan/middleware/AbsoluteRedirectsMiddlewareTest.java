package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.AnyPredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
class AbsoluteRedirectsMiddlewareTest {
    private AbsoluteRedirectsMiddleware<HttpResponse> middleware;
    private HttpRequest request;

    @BeforeEach
    void setup() {
        middleware = new AbsoluteRedirectsMiddleware<>();
        request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders, Headers.of("Host", "example.com"))
                .set(HttpRequest::setScheme, "http")
                .set(HttpRequest::setUri, "/prefix/")
                .set(HttpRequest::setQueryString, "a=b&c=d")
                .build();
    }

    @Test
    void documentRootPath() {
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(new AnyPredicate<>(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .set(HttpResponse::setStatus, 303)
                                .set(HttpResponse::setHeaders, Headers.of("Location", "/foo/bar"))
                                .build());

        HttpResponse response = middleware.handle(request, chain);
        assertThat(response.getHeaders().get("Location"))
                .isEqualTo("http://example.com/foo/bar");
    }

    @Test
    void relativePath() {
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(new AnyPredicate<>(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .set(HttpResponse::setStatus, 303)
                                .set(HttpResponse::setHeaders, Headers.of("Location", "foo/bar"))
                                .build());

        HttpResponse response = middleware.handle(request, chain);
        assertThat(response.getHeaders().get("Location"))
                .isEqualTo("http://example.com/prefix/foo/bar");
    }
}