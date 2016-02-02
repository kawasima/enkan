package kotowari.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.component.TransactionComponent;
import enkan.exception.FalteringEnvironmentException;
import enkan.exception.MisconfigurationException;
import enkan.data.Routable;

import javax.inject.Inject;
import javax.transaction.*;
import java.lang.reflect.Method;

/**
 * @author kawasima
 */
public class TransactionMiddleware<REQ, RES> implements Middleware<REQ, RES> {
    @Inject
    private TransactionComponent transactionComponent;

    private Transactional.TxType getTransactionType(Method m) {
        Transactional transactional = m.getDeclaredAnnotation(Transactional.class);
        return transactional != null ? transactional.value() : null;
    }

    @Override
    public RES handle(REQ req, MiddlewareChain next) {
        RES res = null;
        if (req instanceof Routable) {
            Routable routable = (Routable) req;
            Method m = routable.getControllerMethod();
            Transactional.TxType type= getTransactionType(m);
            if (type != null) {
                TransactionManager tm = transactionComponent.getTransactionManager();
                switch(type) {
                    case REQUIRED:
                        try {
                            tm.begin();
                            res = (RES) next.next(req);
                            tm.commit();
                        } catch (NotSupportedException e) {
                            throw MisconfigurationException.create("TRANSACTION_NOT_SUPPORTED");
                        } catch (SystemException e) {
                            throw FalteringEnvironmentException.create(e);
                        } catch (HeuristicMixedException e) {
                            e.printStackTrace();
                        } catch (HeuristicRollbackException e) {
                            e.printStackTrace();
                        } catch (RollbackException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        throw MisconfigurationException.create("UNSUPPORTED_TX_TYPE");
                }
            } else {
                res = (RES) next.next(req);
            }
        } else {
            res = (RES) next.next(req);
        }
        return res;
    }
}
