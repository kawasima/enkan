package enkan.middleware;

import enkan.collection.Multimap;
import enkan.data.HttpResponse;
import enkan.util.HttpResponseUtils;
import org.junit.Test;

import static enkan.util.BeanBuilder.builder;
import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class DefaultCharsetMiddlewareTest {
    @Test
    public void test() {
        DefaultCharsetMiddleware middleware = new DefaultCharsetMiddleware();
        Multimap<String, Object> headers = Multimap.of("Content-Type", "text/html");
        HttpResponse response = builder(HttpResponse.of("aaa"))
                .set(HttpResponse::setHeaders, headers)
                .build();
        assertNull(HttpResponseUtils.getHeader(response, "Content-Type"));
        middleware.addCharset(response, "UTF-8");
        String type = HttpResponseUtils.getHeader(response, "Content-Type");
        assertNotNull(type);
        assertTrue(type.contains("UTF-8"));
    }
}
