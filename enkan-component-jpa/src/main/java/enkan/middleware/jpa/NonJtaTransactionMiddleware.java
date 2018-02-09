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

@Middleware(name = "nonJtaTransaction", dependencies = "entityManager")
public class NonJtaTransactionMiddleware<REQ, RES> implements enkan.Middleware<REQ,RES> {
    private Transactional.TxType getTransactionType(Method m) {
        Transactional transactional = m.getDeclaredAnnotation(Transactional.class);
        return transactional != null ? transactional.value() : null;
    }

    @Override
    public RES handle(REQ req, MiddlewareChain chain) {
        EntityManager em = Optional.ofNullable(req)
                .filter(EntityManageable.class::isInstance)
                .map(EntityManageable.class::cast)
                .map(EntityManageable::getEntityManager)
                .orElseThrow(() -> new MisconfigurationException("eclipselink.NOT_ENTITY_MANAGEABLE_REQUEST"));
        if (req instanceof Routable) {
            Routable routable = (Routable) req;
                Method m = routable.getControllerMethod();
                Transactional.TxType type = getTransactionType(m);
                if (type != null) {
                    em.getTransaction().begin();
                    try {
                        RES ret = (RES) chain.next(req);
                        em.getTransaction().commit();
                        return ret;
                    } catch (Throwable t) {
                        if (em.getTransaction().isActive()) {
                            em.getTransaction().rollback();
                        }
                        throw t;
                    }
                }
            }
            return (RES) chain.next(req);
    }
}
