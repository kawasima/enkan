package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.AnyPredicate;
import org.junit.Test;

import static enkan.util.BeanBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * {@link CorsMiddleware} Test.
 *
 * @author syobochim
 */
public class CorsMiddlewareTest {

    @Test
    public void testPreflightRequest() throws Exception {
        // SetUp
        HttpRequest request = builder(new DefaultHttpRequest()).build();
        request.setRequestMethod("OPTIONS");
        request.setHeaders(Headers.of("Origin", "http://sample.com",
                "Access-Control-Request-Method", "POST"));

        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(
                new AnyPredicate<>(), null, (Endpoint<HttpRequest, HttpResponse>) req ->
                builder(HttpResponse.of("")).set(HttpResponse::setStatus, 404).build());

        // Exercise
        CorsMiddleware sut = new CorsMiddleware();
        HttpResponse result = sut.handle(request, chain);

        // Verify
        assertThat(result.getStatus(), is(200));
        Headers headers = result.getHeaders();
        assertThat(headers.get("Access-Control-Allow-Origin"), is("*"));
        assertThat(headers.get("Access-Control-Allow-Methods"), containsString("OPTIONS"));
        assertThat(headers.get("Access-Control-Allow-Headers"), containsString("Content-Type"));
        assertThat(headers.get("Access-Control-Allow-Credentials"), is("true"));
    }

    @Test
    public void testAddHeaders() throws Exception {
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
        CorsMiddleware sut = new CorsMiddleware();
        HttpResponse result = sut.handle(request, chain);

        // Verify
        Headers headers = result.getHeaders();
        assertThat(result.getStatus(), is(200));
        assertThat(headers.get("Access-Control-Allow-Origin"), is("*"));
        assertThat(result.getBodyAsString(), is("hello"));
        assertThat(headers.get("Content-Type"), is("text/html"));
    }

    @Test
    public void testNonCORSRequest() throws Exception {
        // Setup
        HttpRequest request = builder(new DefaultHttpRequest()).build();
        request.setHeaders(Headers.of("User-Agent", "Chrome"));

        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(
                new AnyPredicate<>(), null, (Endpoint<HttpRequest, HttpResponse>) req ->
                builder(HttpResponse.of("hello"))
                        .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html")).build());

        // Exercise
        CorsMiddleware sut = new CorsMiddleware();
        HttpResponse result = sut.handle(request, chain);

        // Verify
        Headers headers = result.getHeaders();
        assertThat(headers.get("Access-Control-Allow-Origin"), nullValue());
        assertThat(headers.get("Access-Control-Allow-Methods"), nullValue());
        assertThat(headers.get("Access-Control-Allow-Headers"), nullValue());
        assertThat(headers.get("Access-Control-Allow-Credentials"), nullValue());
        assertThat(result.getBodyAsString(), is("hello"));
        assertThat(headers.get("Content-Type"), is("text/html"));
    }

    @Test
    public void testPreflightRequestWithLowerCaseMethod() throws Exception {
        // SetUp
        HttpRequest request = builder(new DefaultHttpRequest()).build();
        request.setRequestMethod("options");
        request.setHeaders(Headers.of("Origin", "http://sample.com",
                "Access-Control-Request-Method", "POST"));

        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(
                new AnyPredicate<>(), null, (Endpoint<HttpRequest, HttpResponse>) req ->
                builder(HttpResponse.of("")).set(HttpResponse::setStatus, 404).build());

        // Exercise
        CorsMiddleware sut = new CorsMiddleware();
        HttpResponse result = sut.handle(request, chain);

        // Verify
        assertThat(result.getStatus(), is(200));
        Headers headers = result.getHeaders();
        assertThat(headers.get("Access-Control-Allow-Origin"), is("*"));
        assertThat(headers.get("Access-Control-Allow-Methods"), containsString("OPTIONS"));
        assertThat(headers.get("Access-Control-Allow-Headers"), containsString("Content-Type"));
        assertThat(headers.get("Access-Control-Allow-Credentials"), is("true"));
    }

    @Test
    public void testCORSRequestWithLowerCaseMethod() throws Exception {
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
        CorsMiddleware sut = new CorsMiddleware();
        HttpResponse result = sut.handle(request, chain);

        // Verify
        Headers headers = result.getHeaders();
        assertThat(result.getStatus(), is(200));
        assertThat(headers.get("Access-Control-Allow-Origin"), is("*"));
        assertThat(result.getBodyAsString(), is("hello"));
        assertThat(headers.get("Content-Type"), is("text/html"));
    }

}
