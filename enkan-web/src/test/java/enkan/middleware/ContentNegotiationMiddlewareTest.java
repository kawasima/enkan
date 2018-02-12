package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.data.ContentNegotiable;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.AnyPredicate;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Stream;

import static enkan.util.BeanBuilder.builder;
import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class ContentNegotiationMiddlewareTest {
    private ContentNegotiationMiddleware middleware;
    private HttpRequest request;

    @Before
    public void setup() {
        middleware = new ContentNegotiationMiddleware();
    }

    @Test
    public void acceptLanguageWithoutAllowedLanguages() {
        request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders,
                        Headers.of("Accept-Language", "ja,en;q=0.8,en-US;q=0.6"))
                .build();
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(new AnyPredicate<>(), null,
                (Endpoint<HttpRequest, HttpResponse>) req -> {
                    Optional<Locale> locale = Stream.of(req)
                            .map(ContentNegotiable.class::cast)
                            .map(ContentNegotiable::getLocale)
                            .filter(Objects::nonNull)
                            .findFirst();

                    assertFalse(locale.isPresent());
                    return builder(HttpResponse.of("hello"))
                            .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                            .build();
                });
        middleware.handle(request, chain);
    }

    @Test
    public void acceptLanguage() {
        middleware.setAllowedLanguages(new HashSet<>(Collections.singletonList("ja")));
        request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders,
                        Headers.of("Accept-Language", "ja,en;q=0.8,en-US;q=0.6"))
                .build();
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(new AnyPredicate<>(), null,
                (Endpoint<HttpRequest, HttpResponse>) req -> {
                    Optional<Locale> locale = Stream.of(req)
                            .map(ContentNegotiable.class::cast)
                            .map(ContentNegotiable::getLocale)
                            .filter(Objects::nonNull)
                            .findFirst();

                    assertTrue(locale.isPresent());
                    assertEquals(Locale.JAPANESE, locale.get());
                    return builder(HttpResponse.of("hello"))
                            .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                            .build();
                });
        middleware.handle(request, chain);
    }

}