package enkan.middleware;

import enkan.Endpoint;
import enkan.chain.DefaultMiddlewareChain;
import enkan.util.Predicates;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class ServiceUnavailableMiddlewareTest {
    @Test
    public void test() {
        Endpoint<String, String> endpoint = req -> "request: " + req;
        ServiceUnavailableMiddleware middleware = new ServiceUnavailableMiddleware(endpoint);
        Object res = middleware.handle("1", new DefaultMiddlewareChain<>(Predicates.ANY, "",
                (req, chain) -> {
                    fail("Unreachable");
                    return "ok";
                }));
        assertThat(res).isEqualTo("request: 1");
    }
}
