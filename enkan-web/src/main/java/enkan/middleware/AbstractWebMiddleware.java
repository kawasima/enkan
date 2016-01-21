package enkan.middleware;

import enkan.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.UnrecoverableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author kawasima
 */
public abstract class AbstractWebMiddleware implements Middleware<HttpRequest, HttpResponse> {
    private static final Logger MISCONFIG_LOG = LoggerFactory.getLogger("enkan.misconfig");

    protected HttpResponse castToHttpResponse(Object objectResponse) {
        if (objectResponse instanceof HttpResponse) {
            return (HttpResponse) objectResponse;
        } else {
            MISCONFIG_LOG.warn("");
            throw UnrecoverableException.create("");
        }
    }
}
