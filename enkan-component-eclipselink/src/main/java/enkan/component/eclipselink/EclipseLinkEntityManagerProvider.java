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

import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitTransactionType;
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
    private List<Class<?>> managedClasses = new ArrayList<>();

    private String sqlLogLevel = "FINE";

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
                URL dummyPersistenceXmlUrl = getClass().getResource("/META-INF/persistence.xml");
                String s = dummyPersistenceXmlUrl.toExternalForm();
                URI rootUri = URI.create(s.substring(0, s.length() - "persistence.xml".length()));
                try {
                    pu.setPersistenceUnitRootUrl(rootUri.toURL());
                } catch (MalformedURLException e) {
                    throw new UnreachableException(e);
                }
                pu.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
                pu.setNonJtaDataSource(getDataSource());
                List<URL> jarFiles = managedClasses.stream()
                        .map(cls -> cls.getResource("/"))
                        .distinct()
                        .collect(Collectors.toList());
                pu.setJarFileUrls(jarFiles);

                List<String> managedClassNames = managedClasses.stream()
                        .map(Class::getName)
                        .collect(Collectors.toList());
                pu.setManagedClassNames(managedClassNames);
                pu.setExcludeUnlistedClasses(false);

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
     * @param sqllogLevel the logging level
     */
    public void setSqlLogLevel(String sqllogLevel) {
        this.sqlLogLevel = sqlLogLevel;
    }
}
