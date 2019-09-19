package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.AnyPredicate;
import org.junit.jupiter.api.Test;

import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * {@link CorsMiddleware} Test.
 *
 * @author syobochim
 */
class CorsMiddlewareTest {

    @Test
    void testPreflightRequest() {
        // SetUp
        HttpRequest request = builder(new DefaultHttpRequest()).build();
        request.setRequestMethod("OPTIONS");
        request.setHeaders(Headers.of("Origin", "http://sample.com",
                "Access-Control-Request-Method", "POST"));

        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(
                new AnyPredicate<>(), null, (Endpoint<HttpRequest, HttpResponse>) req ->
                builder(HttpResponse.of("")).set(HttpResponse::setStatus, 404).build());

        // Exercise
        CorsMiddleware<HttpResponse> sut = new CorsMiddleware<>();
        HttpResponse result = sut.handle(request, chain);

        // Verify
        assertThat(result.getStatus()).isEqualTo(200);
        Headers headers = result.getHeaders();
        assertThat(headers)
                .contains(entry("Access-Control-Allow-Origin", "*"))
                .contains(entry("Access-Control-Allow-Credentials", "true"));
        assertThat(headers).extracting("Access-Control-Allow-Methods")
                .asString()
                .contains("OPTIONS");
        assertThat(headers).extracting("Access-Control-Allow-Headers")
                .asString()
                .contains("Content-Type");
    }

    @Test
    void testAddHeaders() {
        // SetUp
        HttpRequest request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders, Headers.of("Origin", "http://sample.com"))
                .set(HttpRequest::setRequestMethod, "POST")
                .build();

        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(
                new AnyPredicate<>(), null, (Endpoint<HttpRequest, HttpResponse>) req ->
                builder(HttpResponse.of("hello"))
                        .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html")).build());

        // Exercise
        CorsMiddleware<HttpResponse> sut = new CorsMiddleware<>();
        HttpResponse result = sut.handle(request, chain);

        // Verify
        Headers headers = result.getHeaders();
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(headers)
                .contains(entry("Access-Control-Allow-Origin", "*"),
                        entry("Content-Type", "text/html"));
        assertThat(result.getBodyAsString()).isEqualTo("hello");
    }

    @Test
    void testNonCORSRequest() {
        // Setup
        HttpRequest request = builder(new DefaultHttpRequest()).build();
        request.setHeaders(Headers.of("User-Agent", "Chrome"));

        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(
                new AnyPredicate<>(), null, (Endpoint<HttpRequest, HttpResponse>) req ->
                builder(HttpResponse.of("hello"))
                        .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html")).build());

        // Exercise
        CorsMiddleware<HttpResponse> sut = new CorsMiddleware<>();
        HttpResponse result = sut.handle(request, chain);

        // Verify
        Headers headers = result.getHeaders();
        assertThat(headers)
                .contains(entry("Content-Type", "text/html"))
                .doesNotContainKeys("Access-Control-Allow-Origin",
                        "Access-Control-Allow-Methods",
                        "Access-Control-Allow-Headers",
                        "Access-Control-Allow-Credentials");
        assertThat(result.getBodyAsString())
                .isEqualTo("hello");
    }

    @Test
    void testPreflightRequestWithLowerCaseMethod() {
        // SetUp
        HttpRequest request = builder(new DefaultHttpRequest()).build();
        request.setRequestMethod("options");
        request.setHeaders(Headers.of("Origin", "http://sample.com",
                "Access-Control-Request-Method", "POST"));

        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(
                new AnyPredicate<>(), null, (Endpoint<HttpRequest, HttpResponse>) req ->
                builder(HttpResponse.of("")).set(HttpResponse::setStatus, 404).build());

        // Exercise
        CorsMiddleware<HttpResponse> sut = new CorsMiddleware<>();
        HttpResponse result = sut.handle(request, chain);

        // Verify
        assertThat(result.getStatus()).isEqualTo(200);
        Headers headers = result.getHeaders();

        assertThat(headers)
                .contains(entry("Access-Control-Allow-Origin", "*"))
                .contains(entry("Access-Control-Allow-Credentials", "true"));
        assertThat(headers).extracting("Access-Control-Allow-Methods")
                .asString()
                .contains("OPTIONS");
        assertThat(headers).extracting("Access-Control-Allow-Headers")
                .asString()
                .contains("Content-Type");
    }

    @Test
    void testCORSRequestWithLowerCaseMethod()  {
        // SetUp
        HttpRequest request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders, Headers.of("Origin", "http://sample.com"))
                .set(HttpRequest::setRequestMethod, "post")
                .build();

        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(
                new AnyPredicate<>(), null, (Endpoint<HttpRequest, HttpResponse>) req ->
                builder(HttpResponse.of("hello"))
                        .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html")).build());

        // Exercise
        CorsMiddleware<HttpResponse> sut = new CorsMiddleware<>();
        HttpResponse result = sut.handle(request, chain);

        // Verify
        Headers headers = result.getHeaders();
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(headers)
                .contains(entry("Access-Control-Allow-Origin", "*"),
                        entry("Content-Type", "text/html"));
        assertThat(result.getBodyAsString()).isEqualTo("hello");
    }
}
