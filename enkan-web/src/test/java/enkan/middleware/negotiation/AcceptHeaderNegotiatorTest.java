package enkan.middleware.negotiation;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author kawasima
 */
public class AcceptHeaderNegotiatorTest {
    AcceptHeaderNegotiator neg;

    @Before
    public void setup() {
        neg = new AcceptHeaderNegotiator();
    }
    @Test
    public void acceptFragment() {
        Set<String> allowedTypes = new HashSet<>(Arrays.asList("text/html"));
        MediaType mt = neg.bestAllowedContentType("text/plain; q=0.8", allowedTypes);
        assertEquals("text", mt.getType());
        assertEquals("plain", mt.getSubtype());
    }

    @Test
    public void acceptLanguage() {
        Set<String> allowedLangs = new HashSet<>(Arrays.asList("da", "en-gb", "en"));
        assertEquals("da", neg.bestAllowedLanguage("da, en-gb;q=0.8, en; q=0.7", allowedLangs));

        allowedLangs = new HashSet<>(Arrays.asList("en-gb", "en"));
        assertEquals("en-gb", neg.bestAllowedLanguage("da, en-gb;q=0.8, en; q=0.7", allowedLangs));

        allowedLangs = new HashSet<>(Arrays.asList("en"));
        assertEquals("en", neg.bestAllowedLanguage("da, en-gb;q=0.8, en; q=0.7", allowedLangs));

        allowedLangs = new HashSet<>(Arrays.asList("en-cockney"));
        assertEquals(null, neg.bestAllowedLanguage("da, en-gb;q=0.8", allowedLangs));
    }
}