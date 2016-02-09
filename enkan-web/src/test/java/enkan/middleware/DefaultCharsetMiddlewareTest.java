package enkan.middleware;

import enkan.collection.Headers;
import enkan.data.HttpResponse;
import enkan.util.HttpResponseUtils;
import org.junit.Test;

import static enkan.util.BeanBuilder.builder;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author kawasima
 */
public class DefaultCharsetMiddlewareTest {
    @Test
    public void test() {
        DefaultCharsetMiddleware middleware = new DefaultCharsetMiddleware();
        HttpResponse response = builder(HttpResponse.of("aaa"))
                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                .build();
        assertNotNull(HttpResponseUtils.getHeader(response, "Content-Type"));
        middleware.addCharset(response, "UTF-8");
        String type = HttpResponseUtils.getHeader(response, "Content-Type");
        assertNotNull(type);
        assertTrue(type.contains("UTF-8"));
    }
}
