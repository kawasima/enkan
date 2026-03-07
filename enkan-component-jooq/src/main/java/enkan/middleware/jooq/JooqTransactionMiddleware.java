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
import java.util.Optional;

/**
 * Middleware for jOOQ transaction management.
 *
 * <p>Wraps the downstream handler in a jOOQ transaction when the matched
 * controller class or method is annotated with {@link Transactional}.
 * The transaction is committed on normal return and rolled back on any exception.</p>
 *
 * <p>Annotation lookup order (method-level overrides class-level):
 * <ol>
 *   <li>The controller <strong>class</strong> annotation (if present)</li>
 *   <li>The controller <strong>method</strong> annotation (if present and the method is known)</li>
 * </ol>
 *
 * <p>Only {@link Transactional.TxType#REQUIRED} is supported.
 * Other transaction types will throw {@link MisconfigurationException}.
 * jOOQ nested transactions use savepoints, which do not provide the isolation
 * semantics of {@code REQUIRES_NEW} (separate physical transaction), so
 * claiming support would be misleading.</p>
 *
 * <p>When a transaction is active, a transaction-scoped {@link DSLContext} is stored
 * in the request's {@link Extendable} extensions under the key {@code "jooqDslContext"},
 * replacing any non-transactional DSLContext set by {@link JooqDslContextMiddleware}.</p>
 */
@Middleware(name = "jooqTransaction", dependencies = {"jooqDslContext", "routing"})
public class JooqTransactionMiddleware<REQ, RES> implements DecoratorMiddleware<REQ, RES> {
    @Inject
    private JooqProvider jooqProvider;

    private DSLContext dsl;

    @PostConstruct
    void init() {
        dsl = jooqProvider.getDSLContext();
    }

    private Transactional.TxType getTransactionType(Class<?> cls) {
        Transactional tx = cls.getDeclaredAnnotation(Transactional.class);
        return tx != null ? tx.value() : null;
    }

    private Transactional.TxType getTransactionType(Method m) {
        Transactional tx = m.getDeclaredAnnotation(Transactional.class);
        return tx != null ? tx.value() : null;
    }

    @Override
    public <NNREQ, NNRES> RES handle(REQ req, MiddlewareChain<REQ, RES, NNREQ, NNRES> chain) {
        if (req instanceof Routable routable) {
            Transactional.TxType type = getTransactionType(routable.getControllerClass());
            Method m = routable.getControllerMethod();
            if (m != null) {
                type = Optional.ofNullable(getTransactionType(m)).orElse(type);
            }
            if (type != null) {
                return switch (type) {
                    case REQUIRED -> dsl.transactionResult(ctx -> {
                        if (req instanceof Extendable e) e.setExtension("jooqDslContext", DSL.using(ctx));
                        return chain.next(req);
                    });
                    default -> throw new MisconfigurationException("jooq.UNSUPPORTED_TX_TYPE", type);
                };
            }
        }
        return chain.next(req);
    }
}
