package enkan.middleware.jpa;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.Routable;
import enkan.data.jpa.EntityManageable;
import enkan.exception.MisconfigurationException;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.lang.reflect.Method;
import java.util.Optional;

@Middleware(name = "nonJtaTransaction", dependencies = {"entityManager", "routing"})
public class NonJtaTransactionMiddleware<REQ, RES> implements enkan.Middleware<REQ, RES, REQ, RES> {
    private Transactional.TxType getTransactionType(Class<?> cls) {
        Transactional transactional = cls.getDeclaredAnnotation(Transactional.class);
        return transactional != null ? transactional.value() : null;
    }

    private Transactional.TxType getTransactionType(Method m) {
        Transactional transactional = m.getDeclaredAnnotation(Transactional.class);
        return transactional != null ? transactional.value() : null;
    }

    @Override
    public RES handle(REQ req, MiddlewareChain<REQ, RES, ?, ?> chain) {
        EntityManager em = Optional.ofNullable(req)
                .filter(EntityManageable.class::isInstance)
                .map(EntityManageable.class::cast)
                .map(EntityManageable::getEntityManager)
                .orElseThrow(() -> new MisconfigurationException("jpa.NOT_ENTITY_MANAGEABLE_REQUEST"));
        if (req instanceof Routable) {
            Routable routable = (Routable) req;
            Transactional.TxType type = getTransactionType(routable.getControllerClass());
            Method m = routable.getControllerMethod();
            if (m != null) {
                type = Optional.ofNullable(getTransactionType(m)).orElse(type);
            }
            if (type != null) {
                em.getTransaction().begin();
                try {
                    RES ret = chain.next(req);
                    if (em.getTransaction().getRollbackOnly()) {
                        em.getTransaction().rollback();
                    } else {
                        em.getTransaction().commit();
                    }
                    return ret;
                } catch (Throwable t) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    throw t;
                }
            }
        }
        return chain.next(req);
    }
}
