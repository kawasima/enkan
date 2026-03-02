package enkan.component.eclipselink;

import enkan.component.ComponentLifecycle;
import enkan.component.DataSourceComponent;
import enkan.component.jpa.EntityManagerProvider;
import enkan.exception.UnreachableException;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.logging.slf4j.SLF4JLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * The provider for entity manager by EclipseLink.
 *
 * @author kawasima
 */
public class EclipseLinkEntityManagerProvider extends EntityManagerProvider<EclipseLinkEntityManagerProvider> {
    private static final Logger LOG = LoggerFactory.getLogger(EclipseLinkEntityManagerProvider.class);

    /** Managed classes */
    private final List<Class<?>> managedClasses = new ArrayList<>();

    private String sqlLogLevel = "FINE";

    /**
     * {@inheritDoc}
     */
    @Override
    protected ComponentLifecycle<EclipseLinkEntityManagerProvider> lifecycle() {
        return new ComponentLifecycle<>() {
            @Override
            public void start(EclipseLinkEntityManagerProvider component) {
                component.setDataSourceComponent(component.getDependency(DataSourceComponent.class));
                SEPersistenceUnitInfo pu = new SEPersistenceUnitInfo();
                pu.setPersistenceUnitName(getName());
                pu.setClassLoader(Thread.currentThread().getContextClassLoader());
                URL persistenceXmlUrl = getClass().getResource("/META-INF/persistence.xml");
                try {
                    if (persistenceXmlUrl != null) {
                        String s = persistenceXmlUrl.toExternalForm();
                        URI rootUri = URI.create(s.substring(0, s.length() - "persistence.xml".length()));
                        pu.setPersistenceUnitRootUrl(rootUri.toURL());
                    } else {
                        URL rootUrl = Thread.currentThread().getContextClassLoader().getResource("");
                        if (rootUrl != null) {
                            pu.setPersistenceUnitRootUrl(rootUrl);
                        }
                    }
                } catch (MalformedURLException e) {
                    throw new UnreachableException(e);
                }
                pu.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
                pu.setNonJtaDataSource(getDataSource());

                List<String> managedClassNames = managedClasses.stream()
                        .map(Class::getName)
                        .collect(Collectors.toList());
                pu.setManagedClassNames(managedClassNames);
                pu.setExcludeUnlistedClasses(true);

                getJpaProperties().put(PersistenceUnitProperties.ECLIPSELINK_SE_PUINFO, pu);
                getJpaProperties().put(PersistenceUnitProperties.SESSION_NAME, UUID.randomUUID().toString());
                getJpaProperties().put(PersistenceUnitProperties.LOGGING_LEVEL + ".sql", sqlLogLevel);
                // Bridge to SLF4j
                getJpaProperties().put(PersistenceUnitProperties.LOGGING_LOGGER, SLF4JLogger.class.getName());

                component.setEntityManagerFactory(Persistence
                        .createEntityManagerFactory(getName(), getJpaProperties()));
            }

            @Override
            public void stop(EclipseLinkEntityManagerProvider component) {
                EntityManagerFactory emf = component.getEntityManagerFactory();
                if (emf != null && emf.isOpen()) {
                    emf.close();
                }
            }
        };
    }

    /**
     * Register a class managed by Eclipselink.
     *
     * @param managedClass A class managed by Eclipselink
     */
    public void registerClass(Class<?> managedClass) {
        managedClasses.add(managedClass);
    }

    /**
     * Register classes managed by Eclipselink.
     *
     * @param classes classes managed by Eclipselink
     */
    public void registerClasses(Class<?>... classes) {
        managedClasses.addAll(Arrays.asList(classes));
    }

    /**
     * Set the logging level for executed SQLs.
     *
     * @param sqlLogLevel the logging level
     */
    public void setSqlLogLevel(String sqlLogLevel) {
        this.sqlLogLevel = sqlLogLevel;
    }
}
