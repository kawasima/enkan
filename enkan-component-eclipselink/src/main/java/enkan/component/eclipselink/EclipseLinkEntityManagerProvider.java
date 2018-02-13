package enkan.component.eclipselink;

import enkan.component.ComponentLifecycle;
import enkan.component.DataSourceComponent;
import enkan.component.eclipselink.EntityManagerProvider;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;

import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.util.ArrayList;
import java.util.List;

/**
 * The provider for entity manager by EclipseLink.
 *
 * @author kawasima
 */
public class EclipseLinkEntityManagerProvider extends EntityManagerProvider<EclipseLinkEntityManagerProvider> {

    /** Managed classes */
    private List<String> managedClassNames = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected ComponentLifecycle<EclipseLinkEntityManagerProvider> lifecycle() {
        return new ComponentLifecycle<EclipseLinkEntityManagerProvider>() {
            @Override
            public void start(EclipseLinkEntityManagerProvider component) {
                component.setDataSourceComponent(component.getDependency(DataSourceComponent.class));
                SEPersistenceUnitInfo pu = new SEPersistenceUnitInfo();
                pu.setPersistenceUnitName(getName());
                pu.setClassLoader(Thread.currentThread().getContextClassLoader());
                pu.setPersistenceUnitRootUrl(getClass().getResource("/"));
                pu.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
                pu.setNonJtaDataSource(getDataSource());
                pu.setManagedClassNames(managedClassNames);
                pu.setExcludeUnlistedClasses(false);
                getJpaProperties().put(PersistenceUnitProperties.ECLIPSELINK_SE_PUINFO, pu);
                component.setEntityManagerFactory(Persistence
                        .createEntityManagerFactory(getName(), getJpaProperties()));
            }

            @Override
            public void stop(EclipseLinkEntityManagerProvider component) {

            }
        };
    }

    public void registerClass(Class<?> managedClass) {
        managedClassNames.add(managedClass.getName());
    }
}
