package enkan.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.data.HttpResponse;
import enkan.data.TraceLog;
import enkan.data.Traceable;
import enkan.util.MixinUtils;

/**
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "trace")
public class TraceMiddleware<REQ, RES> implements Middleware<REQ, RES, REQ, RES> {
    @Override
    public RES handle(REQ req, MiddlewareChain<REQ, RES, ?, ?> chain) {
        if (req != null) {
            req = MixinUtils.mixin(req, Traceable.class);
        }
        RES res = chain.next(req);
        if (req instanceof Traceable){
            TraceLog log = ((Traceable) req).getTraceLog();
            log.getEntries().stream()
                    .filter(entry -> res instanceof HttpResponse)
                    .forEach(entry -> ((HttpResponse) res).getHeaders().put("X-Enkan-Trace",
                            entry.getTimestamp() + ":" + entry.getMiddleware()));
        }

        return res;
    }
}
