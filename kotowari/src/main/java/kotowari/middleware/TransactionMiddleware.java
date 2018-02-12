package kotowari.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.component.TransactionComponent;
import enkan.data.Routable;
import enkan.exception.MisconfigurationException;
import enkan.exception.UnreachableException;

import javax.inject.Inject;
import javax.transaction.*;
import java.lang.reflect.Method;

/**
 * @author kawasima
 */
public class TransactionMiddleware<REQ, RES> implements Middleware<REQ, RES, REQ, RES> {
    @Inject
    private TransactionComponent transactionComponent;

    private Transactional.TxType getTransactionType(Method m) {
        Transactional transactional = m.getDeclaredAnnotation(Transactional.class);
        return transactional != null ? transactional.value() : null;
    }

    @Override
    public RES handle(REQ req, MiddlewareChain<REQ, RES, ?, ?> chain) {
        RES res;
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
                            res = chain.next(req);
                            tm.commit();
                        } catch (NotSupportedException e) {
                            throw new UnreachableException(e);
                        } catch (SystemException e) {
                            throw new MisconfigurationException("kotowari.TX_UNEXPECTED_CONDITION", e.errorCode, e);
                        } catch (HeuristicMixedException e) {
                            throw new MisconfigurationException("kotowari.TX_HEURISTIC_MIXED", e.getMessage(), e);
                        } catch (HeuristicRollbackException e) {
                            throw new MisconfigurationException("kotowari.TX_HEURISTIC_ROLLBACK", e.getMessage(), e);
                        } catch (RollbackException e) {
                            throw new MisconfigurationException("kotowari.TX_ROLLBACK", e.getMessage(), e);
                        }
                        break;
                    default:
                        throw new MisconfigurationException("kotowari.UNSUPPORTED_TX_TYPE", type);
                }
            } else {
                res = chain.next(req);
            }
        } else {
            res = chain.next(req);
        }
        return res;
    }
}
