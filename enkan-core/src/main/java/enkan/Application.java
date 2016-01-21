package enkan;

/**
 * The application applied middlewares.
 *
 * @author kawasima
 */
public interface Application<REQ, RES> {
    <M_REQ, M_RES> void use(Middleware<M_REQ, M_RES> middleware);

    <IN, OUT> void use(Decision<IN> decision, Middleware<IN, OUT> middleware);

    void clear();
    RES handle(REQ req);
}
