package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.collection.Parameters;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.AnyPredicate;
import org.junit.Test;

import static enkan.util.BeanBuilder.*;
import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class MethodOverrideMiddlewareTest {
    @Test
    public void defaultIsParameter_method() {
        MethodOverrideMiddleware<HttpResponse> middleware = new MethodOverrideMiddleware<>();
        HttpRequest request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setParams, Parameters.of("_method", "PUT"))
                .build();
        MiddlewareChain<HttpRequest, HttpResponse, ? ,?> chain = new DefaultMiddlewareChain<>(new AnyPredicate<>(), null,
                (Endpoint<HttpRequest, HttpResponse>) req -> {
                    assertEquals("PUT", req.getRequestMethod());
                    return builder(HttpResponse.of("hello")).build();
                });
        middleware.handle(request, chain);
    }

    @Test
    public void overrideUsingByHeader() {
        MethodOverrideMiddleware<HttpResponse> middleware = builder(new MethodOverrideMiddleware<HttpResponse>())
                .set(MethodOverrideMiddleware::setGetterFunction, "X-Override-Method")
                .build();
        HttpRequest request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setParams, Parameters.of("_method", "PUT"))
                .set(HttpRequest::setHeaders, Headers.of("X-Override-Method", "DELETE"))
                .build();
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(new AnyPredicate<>(), null,
                (Endpoint<HttpRequest, HttpResponse>) req -> {
                    assertEquals("DELETE", req.getRequestMethod());
                    return builder(HttpResponse.of("hello")).build();
                });
        middleware.handle(request, chain);
    }
}
