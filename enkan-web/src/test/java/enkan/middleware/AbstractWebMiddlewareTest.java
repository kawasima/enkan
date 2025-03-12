package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author kawasima
 */
class AbstractWebMiddlewareTest {
    @Test
    void test() {
        AbstractWebMiddleware<HttpRequest, String> middleware = new AbstractWebMiddleware<>() {
            @Override
            public <NNREQ, NNRES> HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, String, NNREQ, NNRES> chain) {
                return HttpResponse.of("hello");
            }
        };
        assertThatThrownBy(() -> middleware.castToHttpResponse(""))
                .isInstanceOf(MisconfigurationException.class);
    }
}
