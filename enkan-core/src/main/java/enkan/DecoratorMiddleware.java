package enkan;

/**
 * A middleware that processes a request and response without changing their types.
 *
 * <p>Unlike the general {@link Middleware} interface which permits the next step
 * to use different request/response types ({@code NREQ}/{@code NRES}),
 * a {@code DecoratorMiddleware} constrains {@code NREQ == REQ} and
 * {@code NRES == RES}, reflecting the common pattern where a middleware decorates
 * a request or response (e.g. adding headers, injecting session data) without
 * performing any type conversion.
 *
 * <p>Because Java's type erasure makes it impossible to define a second
 * {@code handle} overload that differs only in the wildcard bounds of
 * {@code MiddlewareChain}, the {@link Middleware#handle handle} method is
 * inherited as-is. Implementors simply use the {@code chain} argument directly —
 * the type constraints on this interface ensure that calling
 * {@code chain.next(req)} returns {@code RES} without any unchecked cast.
 *
 * <p>Typical usage:
 * <pre>{@code
 * public class MyMiddleware<REQ, RES> implements DecoratorMiddleware<REQ, RES> {
 *     @Override
 *     public <NNREQ, NNRES> RES handle(REQ req, MiddlewareChain<REQ, RES, NNREQ, NNRES> chain) {
 *         // decorate req ...
 *         RES res = chain.next(req);   // RES is guaranteed — no cast needed
 *         // decorate res ...
 *         return res;
 *     }
 * }
 * }</pre>
 *
 * @param <REQ> the request type
 * @param <RES> the response type
 * @author kawasima
 */
public interface DecoratorMiddleware<REQ, RES> extends Middleware<REQ, RES, REQ, RES> {
}
