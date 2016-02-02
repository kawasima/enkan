package enkan.component;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;

import static enkan.util.ReflectionUtils.tryReflection;

/**
 * @author kawasima
 */
public class DomaProvider extends SystemComponent {
    private DataSource dataSource;

    public <T> T getDao(Class<? extends T> daoInterface) {
        return tryReflection(() -> {
            Class<? extends T> daoClass = (Class<? extends T>) Class.forName(daoInterface.getName() + "Impl", true, daoInterface.getClassLoader());
            Constructor<? extends T> daoConstructor = daoClass.getConstructor(DataSource.class);
            return daoConstructor.newInstance(dataSource);
        });
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
            }
        };
    }
}
