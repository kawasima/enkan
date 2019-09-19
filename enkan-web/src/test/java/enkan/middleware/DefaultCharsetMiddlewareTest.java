package enkan.middleware;

import enkan.collection.Headers;
import enkan.data.HttpResponse;
import enkan.util.HttpResponseUtils;
import org.junit.jupiter.api.Test;

import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
class DefaultCharsetMiddlewareTest {
    @Test
    void test() {
        DefaultCharsetMiddleware middleware = new DefaultCharsetMiddleware();
        HttpResponse response = builder(HttpResponse.of("aaa"))
                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                .build();
        String contentType = HttpResponseUtils.getHeader(response, "Content-Type");
        assertThat(contentType).isNotNull();
    }

    @Test
    void charset() {
        DefaultCharsetMiddleware middleware = new DefaultCharsetMiddleware();
        HttpResponse response = builder(HttpResponse.of("aaa"))
                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                .build();
        middleware.addCharset(response, "UTF-8");
        String contentType = HttpResponseUtils.getHeader(response, "Content-Type");
        assertThat(contentType)
                .isNotNull()
                .contains("UTF-8");
    }
}
