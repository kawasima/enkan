package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.AnyPredicate;
import org.junit.Before;
import org.junit.Test;

import static enkan.util.BeanBuilder.builder;
import static org.junit.Assert.assertEquals;

/**
 * @author kawasima
 */
public class AbsoluteRedirectsMiddlewareTest {
    private AbsoluteRedirectsMiddleware middleware;
    private HttpRequest request;
    @Before
    public void setup() {
        middleware = new AbsoluteRedirectsMiddleware();
        request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders, Headers.of("Host", "example.com"))
                .set(HttpRequest::setScheme, "http")
                .set(HttpRequest::setUri, "/prefix/")
                .set(HttpRequest::setQueryString, "a=b&c=d")
                .build();
    }

    @Test
    public void docrootPath() {
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain(new AnyPredicate(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .set(HttpResponse::setStatus, 303)
                                .set(HttpResponse::setHeaders, Headers.of("Location", "/foo/bar"))
                                .build());

        HttpResponse response = middleware.handle(request, chain);
        assertEquals("http://example.com/foo/bar", response.getHeaders().get("Location"));
    }

    @Test
    public void relativePath() {
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain(new AnyPredicate(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .set(HttpResponse::setStatus, 303)
                                .set(HttpResponse::setHeaders, Headers.of("Location", "foo/bar"))
                                .build());

        HttpResponse response = middleware.handle(request, chain);
        assertEquals("http://example.com/prefix/foo/bar", response.getHeaders().get("Location"));
    }
}