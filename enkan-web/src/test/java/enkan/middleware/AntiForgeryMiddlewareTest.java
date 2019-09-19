package enkan.middleware;

import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
class AntiForgeryMiddlewareTest {
    private AntiForgeryMiddleware<HttpResponse> middleware;

    @BeforeEach
    void setup() {
        middleware = new AntiForgeryMiddleware<>();
    }

    @Test
    void getSessionToken() {
        HttpRequest request = builder(new DefaultHttpRequest())
                .build();
        assertThat(middleware.sessionToken(request)).isNotPresent();

        Session session = new Session();
        request.setSession(session);
        assertThat(middleware.sessionToken(request)).isNotPresent();

        session.put(AntiForgeryMiddleware.class.getName() + "/antiForgeryToken",
                "token");
        assertThat(middleware.sessionToken(request).get()).isEqualTo("token");
    }
}
