package enkan.middleware.jpa;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.component.jpa.EntityManagerProvider;
import enkan.data.jpa.EntityManageable;
import enkan.util.MixinUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;

@Middleware(name = "entityManager")
public class EntityManagerMiddleware<REQ, RES> implements enkan.Middleware<REQ, RES, REQ, RES> {
    @Inject
    private EntityManagerProvider entityManagerProvider;

    @Override
    public RES handle(REQ req, MiddlewareChain<REQ, RES, ?, ?> chain) {
        EntityManager em = entityManagerProvider.createEntityManager();
        req = MixinUtils.mixin(req, EntityManageable.class);
        EntityManageable.class.cast(req).setEntityManager(em);
        try {
            return chain.next(req);
        } finally {
            em.close();
        }
    }
}
