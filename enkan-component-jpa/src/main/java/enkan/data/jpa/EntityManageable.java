package enkan.data.jpa;

import enkan.data.Extendable;

import jakarta.persistence.EntityManager;

public interface EntityManageable extends Extendable {
    default void setEntityManager(EntityManager entityManager) {
        setExtension("entityManager", entityManager);
    }

    default EntityManager getEntityManager() {
        return getExtension("entityManager");
    }
}
