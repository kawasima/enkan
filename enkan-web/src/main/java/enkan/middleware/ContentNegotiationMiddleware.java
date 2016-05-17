package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.ContentNegotiable;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.negotiation.AcceptHeaderNegotiator;
import enkan.middleware.negotiation.ContentNegotiator;
import enkan.util.MixinUtils;

import javax.ws.rs.core.MediaType;
import java.util.*;

import static enkan.util.ThreadingUtils.some;

/**
 * Accept =&gt; Convert response format.
 *
 * @author kawasima
 */
@Middleware(name = "contentNegotiation")
public class ContentNegotiationMiddleware extends AbstractWebMiddleware {
    private ContentNegotiator negotiator;
    private Set<String> allowedTypes;
    private Set<String> allowedLanguages;

    public ContentNegotiationMiddleware() {
        negotiator = new AcceptHeaderNegotiator();
        allowedTypes = new HashSet<>(Collections.singletonList("text/html"));
        allowedLanguages = new HashSet<>(Collections.singletonList("*"));
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
        String accept = (String) request.getHeaders().getOrDefault("Accept", "*/*");
        MediaType mediaType = negotiator.bestAllowedContentType(accept, allowedTypes);
        String acceptLanguage = (String) request.getHeaders().getOrDefault("Accept-Language", "*");
        String lang = negotiator.bestAllowedLanguage(acceptLanguage, allowedTypes);
        Locale locale = some(lang, Locale::forLanguageTag).orElse(Locale.getDefault());

        request = MixinUtils.mixin(request, ContentNegotiable.class);
        ContentNegotiable.class.cast(request).setAccept(mediaType);
        ContentNegotiable.class.cast(request).setAcceptLanguage(locale);
        return castToHttpResponse(chain.next(request));
    }

    public void setNegotiator(ContentNegotiator negotiator) {
        this.negotiator = negotiator;
    }

    public void setAllowedTypes(Set<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public void setAlowedLanguages(Set<String> allowedLanguages) {
        this.allowedLanguages = allowedLanguages;
    }
}
