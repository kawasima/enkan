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
        ServiceUnavailableMiddleware<String, String> middleware = new ServiceUnavailableMiddleware<>(endpoint);
        String res = middleware.handle("1", new DefaultMiddlewareChain<>(Predicates.any(), "",
                (req, chain) -> {
                    fail("Unreachable");
                    return "ok";
                }));
        assertThat(res).isEqualTo("request: 1");
    }
}
