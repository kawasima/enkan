package enkan.component.doma2;

import enkan.component.ComponentLifecycle;
import enkan.component.DataSourceComponent;
import enkan.component.SystemComponent;
import org.seasar.doma.jdbc.ConfigProvider;
import org.seasar.doma.jdbc.GreedyCacheSqlFileRepository;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static enkan.util.ReflectionUtils.tryReflection;

/**
 * @author kawasima
 */
public class DomaProvider extends SystemComponent {
    private static final Field SQL_FILE_MAP_FIELD;
    static {
        try {
             SQL_FILE_MAP_FIELD = GreedyCacheSqlFileRepository.class.getDeclaredField("sqlFileMap");
        } catch (NoSuchFieldException ex) {
            throw new IllegalStateException(ex);
        }
    }
    private DataSource dataSource;
    private ConcurrentHashMap<String, Object> daoCache = new ConcurrentHashMap<>();

    public <T> T getDao(Class<? extends T> daoInterface) {
        return (T) daoCache.computeIfAbsent(daoInterface.getName(), key ->
                tryReflection(() -> {
                Class<? extends T> daoClass = (Class<? extends T>) Class.forName(daoInterface.getName() + "Impl", true, daoInterface.getClassLoader());
                Constructor<? extends T> daoConstructor = daoClass.getConstructor(DataSource.class);
                return daoConstructor.newInstance(dataSource);
            }));
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<DomaProvider>() {
            @Override
            public void start(DomaProvider component) {
                DataSourceComponent dataSourceComponent = getDependency(DataSourceComponent.class);
                component.dataSource = dataSourceComponent.getDataSource();
            }

            @Override
            public void stop(DomaProvider component) {
                component.dataSource = null;
                daoCache.values().stream()
                        .filter(ConfigProvider.class::isInstance)
                        .map(ConfigProvider.class::cast)
                        .map(ConfigProvider::getConfig)
                        .filter(GreedyCacheSqlFileRepository.class::isInstance)
                        .map(GreedyCacheSqlFileRepository.class::cast)
                        .forEach(repo ->
                                tryReflection(()-> {
                                    ((ConcurrentHashMap) SQL_FILE_MAP_FIELD.get(repo)).clear();
                                    return null;
                                }));
                daoCache.clear();
            }
        };
    }
}
