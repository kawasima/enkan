package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.collection.Parameters;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.util.Predicates;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

import static enkan.util.BeanBuilder.builder;
import static org.junit.Assert.assertEquals;

/**
 * @author kawasima
 */
public class ResourceMiddlewareTest {
    @Test
    public void resouceIsFound() throws URISyntaxException {
        ResourceMiddleware middleware = new ResourceMiddleware();

        HttpRequest request = new DefaultHttpRequest();
        request.setRequestMethod("GET");
        request.setUri("/test.txt");
        request.setParams(Parameters.of("A", " B ", "C", 1));
        MiddlewareChain chain = new DefaultMiddlewareChain(Predicates.ANY, "endpoint", (req, c) ->
                builder(HttpResponse.of("dummy"))
                        .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/plain"))
                        .build());
        HttpResponse response = middleware.handle(request, chain);
        File expected = new File(ClassLoader.getSystemResource("public/test.txt").toURI());
        assertEquals(expected, response.getBody());
    }

}
