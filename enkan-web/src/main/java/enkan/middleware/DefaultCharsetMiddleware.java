package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.util.HttpResponseUtils;

import static enkan.util.HttpResponseUtils.getHeader;

/**
 * Adds default charset to Content-Type header if not.
 *
 * @author kawasima
 */
@Middleware(name = "defaultCharset")
public class DefaultCharsetMiddleware implements WebMiddleware {
    private String defaultCharset = "UTF-8";

    protected boolean isTextBasedContentType(String contentType) {
        if (contentType == null) return false;
        // Media type comparisons are case-insensitive per RFC 7231 §3.1.1.1.
        String lower = contentType.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("text/")
                || lower.startsWith("application/xml")
                || lower.startsWith("application/json")
                || lower.startsWith("application/ld+json")
                || lower.startsWith("application/xhtml+xml");
    }

    protected boolean isContainsCharset(String contentType) {
        if (contentType == null) return false;
        int len = contentType.length();
        for (int i = 0, end = len - 8; i <= end; i++) {
            if (contentType.regionMatches(true, i, "charset=", 0, 8)) {
                return true;
            }
        }
        return false;
    }

    protected void addCharset(HttpResponse response, String charset) {
        String contentType = getHeader(response, "Content-Type");
        if (isTextBasedContentType(contentType)
                && !isContainsCharset(contentType)) {
            HttpResponseUtils.charset(response, charset);
        }
    }

    @Override
    public <NNREQ, NNRES> HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, HttpResponse, NNREQ, NNRES> chain) {
        HttpResponse response = castToHttpResponse(chain.next(request));
        if (response != null) {
            addCharset(response, defaultCharset);
        }
        return response;
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }
}
