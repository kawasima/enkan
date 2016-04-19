package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.util.HttpResponseUtils;
import enkan.util.MimeTypeUtils;

/**
 * Adds default Content-Type header if not.
 *
 * @author kawasima
 */
@Middleware(name = "contentType")
public class ContentTypeMiddleware extends AbstractWebMiddleware {
    protected void contentTypeResponse(HttpResponse response, HttpRequest request) {
        if (HttpResponseUtils.getHeader(response, "Content-Type") == null) {
            String uri = request.getUri();

            String type = MimeTypeUtils.extMimeType(uri);
            if (type == null)
                type = "application/octet-stream";
            HttpResponseUtils.contentType(response, type);
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        HttpResponse response = castToHttpResponse(next.next(request));
        if (response != null) {
            contentTypeResponse(response, request);
        }
        return response;
    }
}
