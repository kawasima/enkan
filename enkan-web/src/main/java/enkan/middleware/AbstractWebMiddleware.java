package enkan.middleware;

import enkan.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;


/**
 * A middleware handles HttpRequest/HttpResponse.
 *
 * @author kawasima
 */
public abstract class AbstractWebMiddleware<NREQ, NRES> implements Middleware<HttpRequest, HttpResponse, NREQ, NRES> {
    /**
     * Cast any response object to HttpResponse.
     *
     * @param objectResponse A response object
     * @return HttpResponse
     */
    protected HttpResponse castToHttpResponse(NRES objectResponse) {
        if (objectResponse == null) {
            return null;
        } else if (objectResponse instanceof HttpResponse) {
            return (HttpResponse) objectResponse;
        } else {
            throw new MisconfigurationException("web.RESPONSE_TYPE_MISMATCH");
        }
    }
}
