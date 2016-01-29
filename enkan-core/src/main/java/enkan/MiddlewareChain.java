package enkan;

import enkan.data.Traceable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kawasima
 */
public class MiddlewareChain<REQ, RES> {
    private static final Logger LOG = LoggerFactory.getLogger("enkan.middleware");

    private Decision<REQ> decision;
    private Middleware<REQ, RES> middleware;
    private String middlewareName;
    private MiddlewareChain<Object, Object> next;


    public MiddlewareChain(Decision<REQ> decision, Middleware<REQ, RES> middleware) {
        this.decision = decision;
        this.middleware = middleware;
        enkan.annotation.Middleware anno = middleware.getClass().getAnnotation(enkan.annotation.Middleware.class);
        if (anno != null) {
            middlewareName = anno.name();
        } else {
            middlewareName = "Anonymous(" + middleware.toString() + ")";
        }
    }

    public MiddlewareChain<REQ, RES> setNext(MiddlewareChain next) {
        this.next = next;
        return this;
    }

    public Middleware<REQ, RES> getMiddleware() {
        return middleware;
    }

    protected void writeTraceLog(Object reqOrRes, String middlewareName) {
        if (reqOrRes instanceof Traceable) {
            ((Traceable) reqOrRes).getTraceLog().write(middlewareName);
        }
    }

    public RES next(REQ req) {
        writeTraceLog(req, middlewareName);

        if (decision.decide(req)) {
            RES res = middleware.handle(req, next);
            writeTraceLog(res, middlewareName);
            return res;
        } else if (next != null){
            RES res = (RES) next.next(req);
            writeTraceLog(res, middlewareName);
            return res;
        } else {
            return null;
        }
    }
}
