package enkan.util.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * A transaction manager for entity manager.
 *
 * @author kawasima
 */
public class EntityTransactionManager {
    private EntityManager em;

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

    public void requiresNew(EntityManager em, Runnable r) {
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
