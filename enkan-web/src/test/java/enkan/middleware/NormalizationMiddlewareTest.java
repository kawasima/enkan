package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.collection.Parameters;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.normalizer.TrimNormalizer;
import enkan.util.Predicates;
import org.junit.Test;

import static enkan.middleware.NormalizationMiddleware.normalization;
import static enkan.util.BeanBuilder.builder;
import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class NormalizationMiddlewareTest {
    @Test
    public void noNormalization() {
        NormalizationMiddleware middleware = new NormalizationMiddleware();
        HttpRequest request = new DefaultHttpRequest();
        request.setParams(Parameters.of("A", "B", "C", 1));
        MiddlewareChain chain = new DefaultMiddlewareChain(Predicates.ANY, "endpoint", (req, c) ->
                builder(HttpResponse.of("dummy"))
                        .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/plain"))
                        .build());
        middleware.handle(request, chain);
        assertEquals("B", request.getParams().get("A"));
    }

    @Test
    public void trimNormalization() {
        NormalizationMiddleware middleware = new NormalizationMiddleware(
                normalization(Predicates.ANY, new TrimNormalizer())
        );
        HttpRequest request = new DefaultHttpRequest();
        request.setParams(Parameters.of("A", " B ", "C", 1));
        MiddlewareChain chain = new DefaultMiddlewareChain(Predicates.ANY, "endpoint", (req, c) ->
                builder(HttpResponse.of("dummy"))
                        .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/plain"))
                        .build());
        middleware.handle(request, chain);
        assertEquals("B", request.getParams().get("A"));
    }

}
