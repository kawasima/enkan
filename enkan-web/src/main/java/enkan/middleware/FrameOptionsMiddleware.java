package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import enkan.util.HttpResponseUtils;

import java.util.Locale;
import java.util.Set;

/**
 * Adds the X-Frame-Options header to the response.
 *
 * <p>Supported values are {@code DENY} and {@code SAMEORIGIN}.
 * The {@code ALLOW-FROM} directive was removed from the specification
 * and is not supported by modern browsers.
 *
 * @author kawasima
 */
@Middleware(name = "frameOptions")
public class FrameOptionsMiddleware implements WebMiddleware {
    private static final Set<String> ALLOWED_VALUES = Set.of("DENY", "SAMEORIGIN");
    private String frameOptions = "SAMEORIGIN";

    @Override
    public <NNREQ, NNRES> HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, HttpResponse, NNREQ, NNRES> chain) {
        HttpResponse response = castToHttpResponse(chain.next(request));
        HttpResponseUtils.header(response, "X-Frame-Options", frameOptions);
        return response;
    }

    /**
     * Set the X-Frame-Options value. Valid values are {@code DENY} and {@code SAMEORIGIN}.
     *
     * @param frameOptions frame options value
     */
    public void setFrameOptions(String frameOptions) {
        String upper = frameOptions.toUpperCase(Locale.US);
        if (!ALLOWED_VALUES.contains(upper)) {
            throw new MisconfigurationException("web.INVALID_FRAME_OPTIONS", frameOptions);
        }
        this.frameOptions = upper;
    }
}
