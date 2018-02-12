package enkan.middleware;

import enkan.Endpoint;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.exception.ServiceUnavailableException;

/**
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "serviceUnavailable")
public class ServiceUnavailableMiddleware<REQ, RES> implements Middleware<REQ, RES, REQ, RES> {
    private Endpoint<REQ, RES> endpoint;

    public ServiceUnavailableMiddleware(Endpoint<REQ, RES> endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public RES handle(REQ req, MiddlewareChain<REQ, RES, ?, ?> next) {
        if (endpoint != null) {
            return endpoint.handle(req);
        }
        throw new ServiceUnavailableException();
    }
}
