package enkan.middleware;

import enkan.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import enkan.exception.UnreachableException;
import enkan.exception.UnrecoverableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author kawasima
 */
public abstract class AbstractWebMiddleware implements Middleware<HttpRequest, HttpResponse> {
    protected HttpResponse castToHttpResponse(Object objectResponse) {
        if (objectResponse instanceof HttpResponse) {
            return (HttpResponse) objectResponse;
        } else {
            MisconfigurationException.raise("RESPONSE_TYPE_MISTMATCH");
            throw UnreachableException.create();
        }
    }
}
