package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.ContentNegotiable;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.negotiation.AcceptHeaderNegotiator;
import enkan.middleware.negotiation.ContentNegotiator;
import enkan.collection.Headers;
import enkan.util.MixinUtils;

import jakarta.ws.rs.core.MediaType;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static enkan.util.ThreadingUtils.*;

/**
 * Accept =&gt; Convert response format.
 *
 * @author kawasima
 */
@Middleware(name = "contentNegotiation", mixins = ContentNegotiable.class)
public class ContentNegotiationMiddleware implements WebMiddleware {
    private ContentNegotiator negotiator;
    private Set<String> allowedTypes;
    private Set<String> allowedLanguages;

    public ContentNegotiationMiddleware() {
        negotiator = new AcceptHeaderNegotiator();
        allowedTypes = Set.of("text/html");
        allowedLanguages = Set.of("*");
    }

    @Override
    public <NNREQ, NNRES> HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, HttpResponse, NNREQ, NNRES> chain) {
        Headers headers = request.getHeaders();
        String accept = headers != null ? Objects.toString(headers.getOrDefault("Accept", "*/*"), "*/*") : "*/*";
        MediaType mediaType = negotiator.bestAllowedContentType(accept, allowedTypes);
        String acceptLanguage = headers != null ? Objects.toString(headers.getOrDefault("Accept-Language", "*"), "*") : "*";
        String lang = negotiator.bestAllowedLanguage(acceptLanguage, allowedLanguages);
        Locale locale = Objects.equals(lang, "*")? null : some(lang, Locale::forLanguageTag).orElse(null);

        request = MixinUtils.mixin(request, ContentNegotiable.class);
        ((ContentNegotiable) request).setMediaType(mediaType);
        ((ContentNegotiable) request).setLocale(locale);
        return castToHttpResponse(chain.next(request));
    }

    public void setNegotiator(ContentNegotiator negotiator) {
        this.negotiator = negotiator;
    }

    public void setAllowedTypes(Set<String> allowedTypes) {
        this.allowedTypes = Set.copyOf(allowedTypes);
    }

    public void setAllowedLanguages(Set<String> allowedLanguages) {
        this.allowedLanguages = Set.copyOf(allowedLanguages);
    }
}
