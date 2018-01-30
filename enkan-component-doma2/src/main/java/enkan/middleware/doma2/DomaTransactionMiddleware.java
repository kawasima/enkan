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

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.lang.reflect.Method;

/**
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "domaTransaction")
public class DomaTransactionMiddleware<REQ, RES> implements Middleware<REQ, RES> {
    @Inject
    private DomaProvider domaProvider;

    private TransactionManager tm;

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
    public RES handle(REQ req, MiddlewareChain next) {
        if (req instanceof Routable) {
            Routable routable = (Routable) req;
            Method m = routable.getControllerMethod();
            Transactional.TxType type= getTransactionType(m);

            if (type != null) {
                switch(type) {
                    case REQUIRED:
                        return tm.required(() ->
                            (RES) next.next(req));
                    case REQUIRES_NEW:
                        return tm.requiresNew(() -> (RES) next.next(req));
                    default:
                        throw new MisconfigurationException("doma2.UNSUPPORTED_TX_TYPE", type);
                }
            }
        }
        return (RES) next.next(req);
    }
}
