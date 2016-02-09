package enkan.predicate;

import enkan.data.UriAvailable;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import static enkan.predicate.PathPredicate.ANY;
import static enkan.predicate.PathPredicate.GET;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author kawasima
 */
public class PathPredicateTest {
    @Data
    @RequiredArgsConstructor
    private static class Request implements UriAvailable {
        @NonNull
        private String uri;
        @NonNull
        private String requestMethod;
    }

    @Test
    public void anyMethod() {
        assertTrue(ANY("/any").test(new Request("/any", "GET")));
        assertTrue(ANY("/any").test(new Request("/any", "POST")));
        assertFalse(ANY("/any").test(new Request("/anyy", "GET")));

        assertTrue(ANY("/any/?.*").test(new Request("/any", "POST")));
        assertFalse(ANY("/any(/?$|/.*)").test(new Request("/anyo", "POST")));
        assertTrue(ANY("/any/?.*").test(new Request("/any/asvao", "POST")));
    }

    @Test
    public void getMethod() {
        assertTrue(GET("/get").test(new Request("/get", "GET")));
        assertFalse(GET("/get").test(new Request("/get", "POST")));
    }


}
