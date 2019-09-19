package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.AnyPredicate;
import enkan.util.HttpResponseUtils;
import org.junit.jupiter.api.Test;

import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
class XssProtectionMiddlewareTest {
    @Test
    void addXssBlockHeader() {
        XssProtectionMiddleware<HttpResponse> middleware = new XssProtectionMiddleware<>();
        HttpRequest request = builder(new DefaultHttpRequest())
                .build();
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain = new DefaultMiddlewareChain<>(new AnyPredicate<>(), null,
                (Endpoint<HttpRequest, HttpResponse>) req -> builder(HttpResponse.of("hello")).build());
        HttpResponse response = middleware.handle(request, chain);
        assertThat((String) HttpResponseUtils.getHeader(response, "X-XSS-Protection"))
                .isEqualTo("1; mode=block");
    }
}
