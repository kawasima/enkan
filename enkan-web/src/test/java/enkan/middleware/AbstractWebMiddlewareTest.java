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
         AbstractWebMiddleware<HttpRequest, String> middleware = new AbstractWebMiddleware<HttpRequest, String>() {
             @Override
             public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, String, ?, ?> chain) {
                 return HttpResponse.of("hello");
             }
         };

        middleware.castToHttpResponse("");
    }
}
