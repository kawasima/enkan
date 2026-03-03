package enkan.middleware.metrics;

import com.codahale.metrics.Timer;
import enkan.DecoratorMiddleware;
import enkan.MiddlewareChain;
import enkan.component.metrics.MetricsComponent;

import jakarta.inject.Inject;

/**
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "metrics")
public class MetricsMiddleware<REQ, RES> implements DecoratorMiddleware<REQ, RES> {
    @Inject
    private MetricsComponent metrics;

    @Override
    public <NNREQ, NNRES> RES handle(REQ req, MiddlewareChain<REQ, RES, NNREQ, NNRES> chain) {
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
