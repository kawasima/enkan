package enkan.middleware.doma2;


import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.component.doma2.DomaPrividerUtils;
import enkan.component.doma2.DomaProvider;
import enkan.data.Routable;
import enkan.exception.MisconfigurationException;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.ConfigSupport;
import org.seasar.doma.jdbc.tx.EnkanLocalTransactionDataSource;
import org.seasar.doma.jdbc.tx.LocalTransactionManager;
import org.seasar.doma.jdbc.tx.TransactionManager;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import jakarta.transaction.Transactional;
import java.lang.reflect.Method;

/**
 * Middleware for Doma2 transaction management.
 * This middleware opens and closes a transaction for each request.
 * The transaction type is determined by the {@link Transactional} annotation.
 * This middleware requires {@link DomaProvider} to provide a {@link Config} object.
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "domaTransaction")
public class DomaTransactionMiddleware<REQ, RES> implements Middleware<REQ, RES, REQ, RES> {
    @Inject
    private DomaProvider domaProvider;

    private TransactionManager tm;

    /**
     * Retrieves the transaction type from the given method.
     *
     * @param m the method to inspect
     * @return the transaction type, or null if not found
     */
    private Transactional.TxType getTransactionType(Method m) {
        Transactional transactional = m.getDeclaredAnnotation(Transactional.class);
        return transactional != null ? transactional.value() : null;
    }

    @PostConstruct
    private void init() {
        Config defaultConfig = DomaPrividerUtils.getDefaultConfig(domaProvider);
        DataSource ds = defaultConfig.getDataSource(); // returns LocalTransactionDataSource
        if (ds instanceof EnkanLocalTransactionDataSource) {
            EnkanLocalTransactionDataSource ltds = (EnkanLocalTransactionDataSource) ds;
            tm = new LocalTransactionManager(ltds.getLocalTransaction(ConfigSupport.defaultJdbcLogger));
        }
    }

    @Override
    public <NRES, NREQ> RES handle(REQ req, MiddlewareChain<REQ, RES, NRES, NREQ> chain) {
        if (req instanceof Routable) {
            Routable routable = (Routable) req;
            Method m = routable.getControllerMethod();
            Transactional.TxType type= getTransactionType(m);

            if (type != null) {
                switch(type) {
                    case REQUIRED:
                        return tm.required(() ->
                            chain.next(req));
                    case REQUIRES_NEW:
                        return tm.requiresNew(() -> chain.next(req));
                    default:
                        throw new MisconfigurationException("doma2.UNSUPPORTED_TX_TYPE", type);
                }
            }
        }
        return chain.next(req);
    }
}
