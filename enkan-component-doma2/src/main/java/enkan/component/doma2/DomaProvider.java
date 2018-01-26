package enkan.component.doma2;

import enkan.component.ComponentLifecycle;
import enkan.component.DataSourceComponent;
import enkan.component.SystemComponent;
import enkan.exception.MisconfigurationException;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.ConfigProvider;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.SqlFileRepository;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.StandardDialect;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

import static enkan.util.ReflectionUtils.*;

/**
 * @author kawasima
 */
public class DomaProvider extends SystemComponent {
    private DataSource dataSource;
    private ConcurrentHashMap<String, Object> daoCache = new ConcurrentHashMap<>();
    private Config defaultConfig;
    private Dialect dialect;
    private Naming naming = Naming.DEFAULT;
    private int maxRows = 0;
    private int fetchSize = 0;
    private int queryTimeout = 0;
    private int batchSize = 0;
    /**
     * Gets the DAO.
     *
     * @param daoInterface a type of DAO
     * @param <T> a type of DAO
     * @return a DAO of the given class.
     */
    public <T> T getDao(Class<? extends T> daoInterface) {
        return (T) daoCache.computeIfAbsent(daoInterface.getName(), key ->
                tryReflection(() -> {
                    Class<? extends T> daoClass;
                    try {
                         daoClass = (Class<? extends T>) Class.forName(daoInterface.getName() + "Impl", true, daoInterface.getClassLoader());
                    } catch (ClassNotFoundException ex) {
                        throw new MisconfigurationException("doma2.DAO_IMPL_NOT_FOUND", daoInterface.getName(), ex);
                    }
                    try {
                        Constructor<? extends T> daoConstructor = daoClass.getConstructor(DataSource.class);
                        return daoConstructor.newInstance(dataSource);
                    } catch (NoSuchMethodException e) {
                        Constructor<? extends T> daoConstructor = daoClass.getConstructor(Config.class);
                        return daoConstructor.newInstance(defaultConfig);
                    }

            }));
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<DomaProvider>() {
            @Override
            public void start(DomaProvider component) {
                DataSourceComponent dataSourceComponent = getDependency(DataSourceComponent.class);
                component.dataSource = dataSourceComponent.getDataSource();
                if (component.dialect == null) component.dialect = new StandardDialect();
                component.defaultConfig = new Config() {
                    @Override
                    public Naming getNaming() {
                        return component.naming;
                    }

                    @Override
                    public DataSource getDataSource() {
                        return component.dataSource;
                    }

                    @Override
                    public Dialect getDialect() {
                        return component.dialect;
                    }

                    @Override
                    public int getMaxRows() {
                        return component.maxRows;
                    }

                    @Override
                    public int getFetchSize() {
                        return component.fetchSize;
                    }

                    @Override
                    public int getQueryTimeout() {
                        return component.queryTimeout;
                    }

                    @Override
                    public int getBatchSize() {
                        return component.batchSize;
                    }
                };
            }

            @Override
            public void stop(DomaProvider component) {
                component.dataSource = null;
                daoCache.values().stream()
                        .filter(ConfigProvider.class::isInstance)
                        .map(ConfigProvider.class::cast)
                        .map(ConfigProvider::getConfig)
                        .map(Config::getSqlFileRepository)
                        .forEach(SqlFileRepository::clearCache);
                daoCache.clear();
            }
        };
    }

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    public void setNaming(Naming naming) {
        this.naming = naming;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
