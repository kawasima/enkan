package enkan.middleware;

import enkan.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;


/**
 * @author kawasima
 */
public abstract class AbstractWebMiddleware implements Middleware<HttpRequest, HttpResponse> {
    protected HttpResponse castToHttpResponse(Object objectResponse) {
        if (objectResponse == null) {
            return null;
        } else if (objectResponse instanceof HttpResponse) {
            return (HttpResponse) objectResponse;
        } else {
            throw MisconfigurationException.create("RESPONSE_TYPE_MISTMATCH");
        }
    }
}
