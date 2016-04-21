package enkan.middleware.negotiation;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author kawasima
 */
public class AcceptHeaderNegotiatorTest {
    @Test
    public void acceptFragment() {
        AcceptHeaderNegotiator neg = new AcceptHeaderNegotiator();
        Set<String> allowedTypes = new HashSet<>(Arrays.asList("text/html"));
        System.out.println(neg.bestAllowedContentType("text/plain; q=0.8", allowedTypes));
    }

}