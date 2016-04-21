package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.ContentNegotiable;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.negotiation.AcceptHeaderNegotiator;
import enkan.middleware.negotiation.ContentNegotiator;
import enkan.util.HttpRequestUtils;
import enkan.util.MixinUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Accept => Convert response format.
 *
 * @author kawasima
 */
@Middleware(name = "contentNegotiation")
public class ContentNegotiationMiddleware extends AbstractWebMiddleware{
    private ContentNegotiator negotiator;
    private Set<String> allowedTypes;

    public ContentNegotiationMiddleware() {
        negotiator = new AcceptHeaderNegotiator();
        allowedTypes = new HashSet<>(Arrays.asList("text/html"));
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
        String accept = (String) request.getHeaders().getOrDefault("Accept", "*/*");
        MediaType mediaType = negotiator.bestAllowedContentType(accept, allowedTypes);
        request = MixinUtils.mixin(request, ContentNegotiable.class);
        ContentNegotiable.class.cast(request).setAccept(mediaType);
        return castToHttpResponse(chain.next(request));
    }

    public void setNegotiator(ContentNegotiator negotiator) {
        this.negotiator = negotiator;
    }
}
