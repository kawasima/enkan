package kotowari.inject.parameter;

import enkan.data.HttpRequest;
import enkan.data.jpa.EntityManageable;
import kotowari.inject.ParameterInjector;

import javax.persistence.EntityManager;
import java.util.Optional;

/**
 * The parameter injector for entity manager.
 *
 * @author kawasima
 */
public class EntityManagerInjector implements ParameterInjector<EntityManager> {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "entityManager";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(Class<?> type, HttpRequest request) {
        return EntityManager.class.isAssignableFrom(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityManager getInjectObject(HttpRequest request) {
        return Optional.ofNullable(request)
                .filter(EntityManageable.class::isInstance)
                .map(EntityManageable.class::cast)
                .map(EntityManageable::getEntityManager)
                .orElse(null);
    }
}
