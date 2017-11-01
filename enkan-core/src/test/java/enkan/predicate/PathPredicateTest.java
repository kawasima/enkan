package enkan.predicate;

import enkan.data.UriAvailable;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static enkan.predicate.PathPredicate.*;
import static org.assertj.core.api.Assertions.*;

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
        assertThat(ANY("/any").test(new Request("/any", "GET"))).isTrue();
        assertThat(ANY("/any").test(new Request("/any", "POST"))).isTrue();
        assertThat(ANY("/any").test(new Request("/anyy", "GET"))).isFalse();

        assertThat(ANY("/any/?.*").test(new Request("/any", "POST"))).isTrue();
        assertThat(ANY("/any(/?$|/.*)").test(new Request("/anyo", "POST"))).isFalse();
        assertThat(ANY("/any/?.*").test(new Request("/any/asvao", "POST"))).isTrue();
    }

    @Test
    public void getMethod() {
        assertThat(GET("/get").test(new Request("/get", "GET"))).isTrue();
        assertThat(GET("/get").test(new Request("/get", "POST"))).isFalse();
    }
}
