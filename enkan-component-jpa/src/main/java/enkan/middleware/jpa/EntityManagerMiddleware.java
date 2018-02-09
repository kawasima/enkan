package enkan.middleware.jpa;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.component.eclipselink.EntityManagerProvider;
import enkan.data.jpa.EntityManageable;
import enkan.util.MixinUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;

@Middleware(name = "entityManager")
public class EntityManagerMiddleware<REQ, RES> implements enkan.Middleware<REQ, RES> {
    @Inject
    private EntityManagerProvider entityManagerProvider;

    @Override
    public RES handle(REQ req, MiddlewareChain chain) {
        EntityManager em = entityManagerProvider.createEntityManager();
        EntityManageable entityManageable = (EntityManageable) MixinUtils.mixin(req, EntityManageable.class);
        entityManageable.setEntityManager(em);
        try {
            return (RES) chain.next(entityManageable);
        } finally {
            em.close();
        }
    }
}
