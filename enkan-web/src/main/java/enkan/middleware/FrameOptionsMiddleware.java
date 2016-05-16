package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Parameters;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import enkan.util.HttpResponseUtils;

import java.util.Locale;

/**
 * Adds the X-Frame-Options header to the response.
 *
 * @author kawasima
 */
@Middleware(name = "frameOptions")
public class FrameOptionsMiddleware extends AbstractWebMiddleware {
    private Object frameOptions;


    public FrameOptionsMiddleware() {
        frameOptions = "sameorigin";
    }

    private String formatFrameOptions() {
        if (frameOptions instanceof Parameters) {
            return "ALLOW-FROM " + ((Parameters) frameOptions).get("allow-from");
        } else {
            return frameOptions.toString().toUpperCase(Locale.US);
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
        String headerValue = formatFrameOptions();
        HttpResponse response = castToHttpResponse(chain.next(request));
        HttpResponseUtils.header(response, "X-Frame-Options", headerValue);
        return response;
    }

    public void setFrameOptions(String frameOptions) {
        this.frameOptions = frameOptions;
    }

    public void setFrameOptions(Parameters frameOptions) {
        if (!frameOptions.containsKey("allow-from")) {
            throw new MisconfigurationException("web.ILLEGAL_FRAME_OPTIONS");
        }
        this.frameOptions = frameOptions;
    }

}
