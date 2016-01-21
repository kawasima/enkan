package enkan.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.TraceLog;
import enkan.data.Traceable;
import enkan.util.HttpRequestUtils;
import enkan.util.MixinUtils;

/**
 * @author kawasima
 */
public class TraceMiddleware<REQ, RES> implements Middleware<REQ, RES> {
    @Override
    public RES handle(REQ req, MiddlewareChain next) {
        if (req instanceof HttpRequest) {
            req = (REQ) MixinUtils.mixin((HttpRequest) req, Traceable.class);
        }
        RES res = (RES) next.next(req);
        if (req instanceof Traceable){
            TraceLog log = ((Traceable) req).getTraceLog();
            for (TraceLog.Entry entry : log.getEntries()) {
                if (res instanceof HttpResponse) {
                    ((HttpResponse) res).getHeaders().put("X-Enkan-Trace",
                            entry.getTimestamp() +":" + entry.getMiddleware());
                }
            }
        }

        return res;
    }
}
