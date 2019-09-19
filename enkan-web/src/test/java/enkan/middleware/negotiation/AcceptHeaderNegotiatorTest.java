package enkan.middleware.negotiation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
class AcceptHeaderNegotiatorTest {
    private AcceptHeaderNegotiator neg;

    @BeforeEach
    void setup() {
        neg = new AcceptHeaderNegotiator();
    }

    @Test
    void acceptFragment() {
        Set<String> allowedTypes = new HashSet<>(Collections.singletonList("text/html"));
        MediaType mt = neg.bestAllowedContentType("text/plain; q=0.8", allowedTypes);
        assertThat(mt.getType()).isEqualTo("text");
        assertThat(mt.getSubtype()).isEqualTo("plain");
    }

    @Test
    void acceptLanguage() {
        Set<String> allowedLangs = new HashSet<>(Arrays.asList("da", "en-gb", "en"));
        assertThat(neg.bestAllowedLanguage("da, en-gb;q=0.8, en; q=0.7", allowedLangs))
                .isEqualTo("da");

        allowedLangs = new HashSet<>(Arrays.asList("en-gb", "en"));
        assertThat(neg.bestAllowedLanguage("da, en-gb;q=0.8, en; q=0.7", allowedLangs))
                .isEqualTo("en-gb");

        allowedLangs = new HashSet<>(Collections.singletonList("en"));
        assertThat(neg.bestAllowedLanguage("da, en-gb;q=0.8, en; q=0.7", allowedLangs))
                .isEqualTo("en");

        allowedLangs = new HashSet<>(Collections.singletonList("en-cockney"));
        assertThat(neg.bestAllowedLanguage("da, en-gb;q=0.8", allowedLangs))
                .isNull();
    }
}
