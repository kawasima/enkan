package enkan.middleware.metrics;

import com.codahale.metrics.Timer;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.component.metrics.MetricsComponent;

import javax.inject.Inject;

/**
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "metrics")
public class MetricsMiddleware<REQ, RES> implements Middleware<REQ, RES, REQ, RES> {
    @Inject
    private MetricsComponent metrics;

    @Override
    public RES handle(REQ req, MiddlewareChain<REQ, RES, ?, ?> chain) {
        Timer.Context context = metrics.getRequestTimer().time();
        metrics.getActiveRequests().inc();

        try {
            return chain.next(req);
        } catch (Exception ex) {
            metrics.getErrors().mark();
            throw ex;
        } finally {
            metrics.getActiveRequests().dec();
            context.stop();
        }
    }
}
