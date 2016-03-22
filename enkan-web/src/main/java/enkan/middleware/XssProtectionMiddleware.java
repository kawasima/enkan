package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import static enkan.util.HttpResponseUtils.header;

/**
 * @author kawasima
 */
@Middleware(name = "xssProtection")
public class XssProtectionMiddleware extends AbstractWebMiddleware {
    private String headerValue;

    public XssProtectionMiddleware() {
        this(true, OptionMap.of("mode", "block"));
    }

    public XssProtectionMiddleware(boolean enable, OptionMap options) {
        StringBuilder sb = new StringBuilder();
        sb.append(enable? "1" : "0");
        String mode = options.getString("mode");
        if (mode != null) {
            sb.append("; mode=").append(mode);
        }
        headerValue = sb.toString();
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest, MiddlewareChain next) {
        HttpResponse response = (HttpResponse) next.next(httpRequest);
        if (response != null) {
            header(response, "X-XSS-Protection", headerValue);
        }
        return response;
    }
}
