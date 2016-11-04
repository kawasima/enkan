package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Headers;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

/**
 * CORS setting.
 *
 * @author syobochim
 */
@Middleware(name = "cors")
public class CORSMiddleware extends AbstractWebMiddleware {
    @Override
    public HttpResponse handle(HttpRequest httpRequest, MiddlewareChain chain) {
        HttpResponse httpResponse = castToHttpResponse(chain.next(httpRequest));
        httpResponse.setHeaders(Headers.of("Access-Control-Allow-Origin", httpRequest.getHeaders().get("Origin"),
                "Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS",
                "Access-Control-Allow-Headers", "Content-Type",
                "Access-Control-Allow-Credentials", "true"));
        return httpResponse;
    }
}
