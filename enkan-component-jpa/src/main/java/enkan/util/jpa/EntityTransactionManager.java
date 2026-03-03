package enkan.util.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * A transaction manager for entity manager.
 *
 * @author kawasima
 */
public class EntityTransactionManager {
    private final EntityManager em;

    public EntityTransactionManager(EntityManager em) {
        this.em = em;
    }

    public void required(Runnable r) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            r.run();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

}
