package enkan.middleware.jpa;

import enkan.DecoratorMiddleware;
import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.component.jpa.EntityManagerProvider;
import enkan.data.jpa.EntityManageable;
import enkan.util.MixinUtils;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@Middleware(name = "entityManager", mixins = EntityManageable.class)
public class EntityManagerMiddleware<REQ, RES> implements DecoratorMiddleware<REQ, RES> {
    @Inject
    private EntityManagerProvider<?> entityManagerProvider;

    @Override
    public <NNREQ, NNRES> RES handle(REQ req, MiddlewareChain<REQ, RES, NNREQ, NNRES> chain) {
        EntityManager em = entityManagerProvider.createEntityManager();
        try (em) {
            req = MixinUtils.mixin(req, EntityManageable.class);
            ((EntityManageable) req).setEntityManager(em);
            return chain.next(req);
        }
    }
}
