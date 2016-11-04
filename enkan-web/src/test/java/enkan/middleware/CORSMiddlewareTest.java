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

import static enkan.util.BeanBuilder.builder;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * {@link CORSMiddleware} Test.
 *
 * @author syobochim
 */
public class CORSMiddlewareTest {

    @Test
    public void test() throws Exception {
        HttpRequest request = builder(new DefaultHttpRequest()).build();
        request.setHeaders(Headers.of("Origin", "http://sample.com"));
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain<>(
                new AnyPredicate<>(), null, (Endpoint<HttpRequest, HttpResponse>) req ->
                builder(HttpResponse.of("hello")).build());

        // Exercise
        CORSMiddleware sut = new CORSMiddleware();
        HttpResponse result = sut.handle(request, chain);

        // Verify
        Headers headers = result.getHeaders();
        assertThat(headers.get("Access-Control-Allow-Origin"), is("http://sample.com"));
        assertThat(headers.get("Access-Control-Allow-Methods"), is("GET, POST, DELETE, PUT, OPTIONS"));
        assertThat(headers.get("Access-Control-Allow-Headers"), is("Content-Type"));
        assertThat(headers.get("Access-Control-Allow-Credentials"), is("true"));
    }

}