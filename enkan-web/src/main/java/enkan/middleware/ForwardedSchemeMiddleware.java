package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import java.util.Locale;

import static enkan.util.ThreadingUtils.some;

/**
 * @author kawasima
 */
@Middleware(name = "forwardedScheme")
public class ForwardedSchemeMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    private String header = "x-forwarded-proto";

    private HttpRequest forwardedSchemeRequest(HttpRequest request) {
        String scheme = some(request.getHeaders().get(header.toLowerCase(Locale.US)),
                String::toLowerCase).orElse(null);

        if ("http".equals(scheme) || "https".equals(scheme)) {
            request.setScheme(scheme);
        }
        return request;
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> chain) {
        return castToHttpResponse(chain.next(forwardedSchemeRequest(request)));
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
