package enkan.middleware.negotiation;

import javax.ws.rs.core.MediaType;
import java.util.Set;

/**
 * @author kawasima
 */
public interface ContentNegotiator {
    MediaType bestAllowedContentType(String accept, Set<String> allowedTypes);
}
