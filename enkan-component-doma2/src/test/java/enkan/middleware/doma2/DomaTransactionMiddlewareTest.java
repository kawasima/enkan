package enkan.middleware.doma2;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Routable;
import enkan.exception.MisconfigurationException;
import enkan.predicate.AnyPredicate;
import enkan.util.MixinUtils;
import org.junit.Test;

import javax.transaction.Transactional;

import java.lang.reflect.Method;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.ReflectionUtils.tryReflection;

/**
 * @author kawasima
 */
public class DomaTransactionMiddlewareTest {
    @Test(expected = MisconfigurationException.class)
    public void configuration() {
        DomaTransactionMiddleware middleware = new DomaTransactionMiddleware<>();
        HttpRequest request = MixinUtils.mixin(new DefaultHttpRequest(), Routable.class);
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain(new AnyPredicate(), null,
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        builder(HttpResponse.of("hello"))
                                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                                .build());

        HttpResponse res = (HttpResponse) tryReflection(() -> {
            Method controllerMethod = TestController.class.getMethod("index");
            Routable.class.cast(request).setControllerMethod(controllerMethod);
            return middleware.handle(request, chain);
        });
    }

    private static class TestController {
        @Transactional(Transactional.TxType.MANDATORY)
        public String index() {
            return "";
        }
    }
}
