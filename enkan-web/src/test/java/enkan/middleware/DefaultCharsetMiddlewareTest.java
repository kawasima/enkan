package enkan.middleware;

import enkan.data.HttpResponse;
import enkan.util.HttpResponseUtils;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;
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
        MutableMultimap<String, Object> headers = Multimaps.mutable.list.with("Content-Type", "text/html");
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
