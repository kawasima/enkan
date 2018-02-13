package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import static enkan.util.HttpResponseUtils.*;

/**
 * Adds X-Content-Type-Options header to the response.
 *
 * @author kawasima
 */
@Middleware(name = "contentTypeOptions")
public class ContentTypeOptionsMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    private String contentTypeOptions = "nosniff";

    public ContentTypeOptionsMiddleware() {

    }

    public ContentTypeOptionsMiddleware(String contentTypeOptions) {
        this.contentTypeOptions = contentTypeOptions;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest, MiddlewareChain<HttpRequest, NRES, ?, ?> next) {
        HttpResponse response = castToHttpResponse(next.next(httpRequest));
        if (response != null) {
            header(response, "X-Content-Type-Options", contentTypeOptions);
        }
        return response;
    }
}
