package enkan.data.jpa;

import enkan.data.Extendable;

import javax.persistence.EntityManager;

public interface EntityManageable extends Extendable {
    default void setEntityManager(EntityManager entityManager) {
        setExtension("entityManager", entityManager);
    }

    default EntityManager getEntityManager() {
        return (EntityManager) getExtension("entityManager");
    }
}
