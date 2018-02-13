package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.data.*;
import enkan.util.MixinUtils;
import enkan.util.Predicates;
import org.junit.Before;
import org.junit.Test;

import static enkan.util.BeanBuilder.builder;
import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class FlashMiddlewareTest {
    private FlashMiddleware middleware;
    private HttpRequest request;
    @Before
    public void setup() {
        Session session = new Session();
        session.put("_flash", new Flash<>("message"));
        middleware = new FlashMiddleware();
        request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setSession, session)
                .build();
    }

    @Test
    public void getFlash_and_NoResponseFlash() {
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(Predicates.any(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                                .build());
        request = MixinUtils.mixin(request, FlashAvailable.class);
        HttpResponse response = middleware.handle(request, chain);
        assertEquals("message", request.getFlash().getValue());
        assertNotNull(request.getSession());
        assertFalse(request.getSession().containsKey("_flash"));
        assertNull(response.getFlash());

    }

    @Test
    public void setFlash() {
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(Predicates.any(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                                .set(HttpResponse::setFlash, new Flash<>("new flash"))
                                .build());
        HttpResponse response = middleware.handle(request, chain);
        assertEquals("message", request.getFlash().getValue());
        assertEquals("new flash", response.getFlash().getValue());
    }

}