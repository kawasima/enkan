package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import org.junit.Test;

/**
 * @author kawasima
 */
public class AbstractWebMiddlewareTest {
    @Test(expected = MisconfigurationException.class)
    public void test() {
         AbstractWebMiddleware middleware = new AbstractWebMiddleware() {
             @Override
             public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
                 return HttpResponse.of("hello");
             }
         };

        middleware.castToHttpResponse("");
    }
}
