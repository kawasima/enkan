package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.collection.Parameters;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.util.Predicates;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;

import static enkan.util.BeanBuilder.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class ResourceMiddlewareTest {
    @Test
    public void resourceIsFound() throws URISyntaxException {
        ResourceMiddleware<HttpResponse> middleware = new ResourceMiddleware<>();

        HttpRequest request = new DefaultHttpRequest();
        request.setRequestMethod("GET");
        request.setUri("/assets/test.txt");
        request.setParams(Parameters.of("A", " B ", "C", 1));
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(Predicates.any(), "endpoint", (req, c) ->
                builder(HttpResponse.of("dummy"))
                        .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/plain"))
                        .build());
        HttpResponse response = middleware.handle(request, chain);
        assertThat(response.getBody())
                .isEqualTo(new File(ClassLoader.getSystemResource("public/test.txt").toURI()));
    }

    @Test
    public void resourceAssetPath() throws URISyntaxException {
        ResourceMiddleware<HttpResponse> middleware = builder(new ResourceMiddleware<HttpResponse>())
                .set(ResourceMiddleware::setUriPrefix, (String) null)
                .build();

        HttpRequest request = new DefaultHttpRequest();
        request.setRequestMethod("GET");
        request.setUri("/test.txt");
        request.setParams(Parameters.of("A", " B ", "C", 1));
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(Predicates.any(), "endpoint", (req, c) ->
                builder(HttpResponse.of("dummy"))
                        .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/plain"))
                        .build());
        HttpResponse response = middleware.handle(request, chain);
        assertThat(response.getBody())
                .isEqualTo(new File(ClassLoader.getSystemResource("public/test.txt").toURI()));
    }

}
