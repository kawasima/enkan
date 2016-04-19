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
import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class FrameOptionsMiddlewareTest {
    private FrameOptionsMiddleware middleware;
    private HttpRequest request;

    @Before
    public void setup() {
        middleware = new FrameOptionsMiddleware();
        request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders,
                        Headers.of("Host", "example.com"))
                .set(HttpRequest::setScheme, "http")
                .set(HttpRequest::setUri, "/prefix/")
                .set(HttpRequest::setQueryString, "a=b&c=d")
                .build();
    }

    @Test
    public void defaultValueIsSameOrigin() {
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain(new AnyPredicate(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                                .build());
        HttpResponse response = middleware.handle(request, chain);
        assertEquals("SAMEORIGIN", response.getHeaders().get("X-Frame-Options"));
    }

    @Test
    public void deny() {
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain(new AnyPredicate(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                                .build());
        middleware.setFrameOptions("DENY");
        HttpResponse response = middleware.handle(request, chain);
        assertEquals("DENY", response.getHeaders().get("X-Frame-Options"));
    }
}