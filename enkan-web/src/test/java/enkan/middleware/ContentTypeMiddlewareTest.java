package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.AnyPredicate;
import enkan.util.HttpResponseUtils;
import org.junit.Before;
import org.junit.Test;

import static enkan.util.BeanBuilder.builder;
import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class ContentTypeMiddlewareTest {
    private ContentTypeMiddleware middleware;
    private HttpRequest request;
    @Before
    public void setup() {
        middleware = new ContentTypeMiddleware();
        request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders, Headers.of("Host", "example.com"))
                .set(HttpRequest::setScheme, "http")
                .set(HttpRequest::setUri, "/prefix/")
                .set(HttpRequest::setQueryString, "a=b&c=d")
                .build();
    }

    @Test
    public void testSetContentType() {
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain(new AnyPredicate(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                                .build());

        HttpResponse response = middleware.handle(request, chain);
        assertEquals("text/html", HttpResponseUtils.getHeader(response, "content-type"));
    }

    @Test
    public void defaultValueIsOctetStream() {
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain(new AnyPredicate(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .build());

        HttpResponse response = middleware.handle(request, chain);
        assertEquals("text/plain", HttpResponseUtils.getHeader(response, "content-type"));
    }

    @Test
    public void testRedirect() {
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain(new AnyPredicate(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .set(HttpResponse::setStatus, 303)
                                .set(HttpResponse::setHeaders, Headers.of("Location", "foo/bar"))
                                .build());

        HttpResponse response = middleware.handle(request, chain);
        assertEquals("This is a specification of ring content_type middleware",
                "text/plain", HttpResponseUtils.getHeader(response, "content-type"));
    }
}
