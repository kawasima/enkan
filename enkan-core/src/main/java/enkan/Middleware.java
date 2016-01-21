package enkan;

/**
 * @author kawasima
 */
public interface Middleware<REQ, RES> {
    RES handle(REQ req, MiddlewareChain next);
}
