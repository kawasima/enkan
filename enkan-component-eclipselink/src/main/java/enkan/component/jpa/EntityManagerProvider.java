package enkan.component.jpa;

import enkan.component.ComponentLifecycle;
import enkan.component.DataSourceComponent;
import enkan.component.SystemComponent;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;

public class EntityManagerProvider extends SystemComponent {
    private String name;
    @Inject
    private DataSourceComponent dataSourceComponent;

    private List<String> managedClassNames = new ArrayList<>();

    private Map<String, Object> jpaProperties = new HashMap<>();

    private EntityManagerFactory entityManagerFactory;
    public EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }
    @Override
    protected ComponentLifecycle<EntityManagerProvider> lifecycle() {
        return new ComponentLifecycle<EntityManagerProvider>() {
            @Override
            public void start(EntityManagerProvider component) {
                SEPersistenceUnitInfo pu = new SEPersistenceUnitInfo();
                pu.setPersistenceUnitName(name);
                pu.setClassLoader(Thread.currentThread().getContextClassLoader());
                pu.setPersistenceUnitRootUrl(getClass().getResource("/"));
                pu.setNonJtaDataSource(dataSourceComponent.getDataSource());
                pu.setManagedClassNames(managedClassNames);
                pu.setExcludeUnlistedClasses(false);
                jpaProperties.put(PersistenceUnitProperties.ECLIPSELINK_SE_PUINFO, pu);
                component.entityManagerFactory = Persistence.createEntityManagerFactory(name, jpaProperties);
            }

            @Override
            public void stop(EntityManagerProvider component) {

            }
        };
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setJpaProperties(Map<String, Object> jpaProperties) {
        this.jpaProperties = jpaProperties;
    }

    public void registerClass(Class<?> managedClass) {
        managedClassNames.add(managedClass.getName());
    }
}
