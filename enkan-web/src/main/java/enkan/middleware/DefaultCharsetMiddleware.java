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
public class DefaultCharsetMiddleware extends AbstractWebMiddleware {
    private String defaultCharset = "UTF-8";

    protected boolean isTextBasedContentType(String contentType) {
        return contentType != null && (contentType.startsWith("text/") || contentType.startsWith("application/xml"));
    }

    protected boolean isContainsCharset(String contentType) {
        return contentType != null && contentType.matches(";\\s*charset=[^;]*");
    }

    protected void addCharset(HttpResponse response, String charset) {
        String contentType = getHeader(response, "Content-Type");
        if (isTextBasedContentType(contentType)
                && !isContainsCharset(contentType)) {
            HttpResponseUtils.charset(response, charset);
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        HttpResponse response = castToHttpResponse(next.next(request));
        if (response != null) {
            addCharset(response, defaultCharset);
        }
        return response;
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }
}
