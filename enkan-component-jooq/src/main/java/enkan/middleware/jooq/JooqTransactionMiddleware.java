package enkan.middleware.jooq;

import enkan.DecoratorMiddleware;
import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.component.jooq.JooqProvider;
import enkan.data.Extendable;
import enkan.data.Routable;
import enkan.exception.MisconfigurationException;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.lang.reflect.Method;

/**
 * Middleware for jOOQ transaction management.
 *
 * <p>Wraps the downstream handler in a jOOQ transaction when the matched
 * controller method is annotated with {@link Transactional}.
 * The transaction is committed on normal return and rolled back on any exception.</p>
 *
 * <p>Supported transaction types:</p>
 * <ul>
 *   <li>{@link Transactional.TxType#REQUIRED} – joins or starts a transaction</li>
 *   <li>{@link Transactional.TxType#REQUIRES_NEW} – always starts a new nested transaction</li>
 * </ul>
 *
 * <p>If the controller method has no {@link Transactional} annotation the request
 * passes through without any transaction wrapper.</p>
 *
 * <p>When a transaction is active, a transaction-scoped {@link DSLContext} is stored
 * in the request's {@link Extendable} extensions under the key {@code "jooqDslContext"}.
 * Controllers and downstream middlewares can retrieve it via
 * {@code request.getExtension("jooqDslContext")} to participate in the same transaction.</p>
 */
@Middleware(name = "jooqTransaction")
public class JooqTransactionMiddleware<REQ, RES> implements DecoratorMiddleware<REQ, RES> {
    @Inject
    private JooqProvider jooqProvider;

    private DSLContext dsl;

    @PostConstruct
    private void init() {
        dsl = jooqProvider.getDSLContext();
    }

    private Transactional.TxType getTransactionType(Method m) {
        Transactional tx = m.getDeclaredAnnotation(Transactional.class);
        return tx != null ? tx.value() : null;
    }

    @Override
    public <NNREQ, NNRES> RES handle(REQ req, MiddlewareChain<REQ, RES, NNREQ, NNRES> chain) {
        if (req instanceof Routable routable) {
            Transactional.TxType type = getTransactionType(routable.getControllerMethod());
            if (type != null) {
                return switch (type) {
                    case REQUIRED -> dsl.transactionResult(ctx -> {
                        if (req instanceof Extendable e) e.setExtension("jooqDslContext", DSL.using(ctx));
                        return chain.next(req);
                    });
                    case REQUIRES_NEW -> dsl.transactionResult(outer ->
                            DSL.using(outer).transactionResult(ctx -> {
                                if (req instanceof Extendable e) e.setExtension("jooqDslContext", DSL.using(ctx));
                                return chain.next(req);
                            }));
                    default -> throw new MisconfigurationException("jooq.UNSUPPORTED_TX_TYPE", type);
                };
            }
        }
        return chain.next(req);
    }
}
