package enkan.middleware;

import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.Session;
import org.junit.Before;
import org.junit.Test;

import static enkan.util.BeanBuilder.builder;
import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class AntiForgeryMiddlewareTest {
    AntiForgeryMiddleware middleware;

    @Before
    public void setup() {
        middleware = new AntiForgeryMiddleware();
    }

    @Test
    public void getSessionToken() {
        HttpRequest request = builder(new DefaultHttpRequest())
                .build();
        assertFalse(middleware.sessionToken(request).isPresent());

        Session session = new Session();
        request.setSession(session);
        assertFalse(middleware.sessionToken(request).isPresent());

        session.setAttribute(AntiForgeryMiddleware.class.getName() + "/antiForgeryToken",
                "token");
        assertEquals("token", middleware.sessionToken(request).get());
    }
}