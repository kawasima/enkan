package enkan.component;

import enkan.exception.UnrecoverableException;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static enkan.util.ReflectionUtils.tryReflection;

/**
 * @author kawasima
 */
public class DomaDaoProvider extends SystemComponent {
    private DataSourceComponent dataSourceComponent;

    public <T> T get(Class<? extends T> daoInterface) {
        return tryReflection(() -> {
            Class<? extends T> daoClass = (Class<? extends T>) Class.forName(daoInterface.getName() + "Impl", true, daoInterface.getClassLoader());
            Constructor<? extends T> daoConstructor = daoClass.getConstructor(DataSource.class);
            return daoConstructor.newInstance(dataSourceComponent.getDataSource());
        });
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<DomaDaoProvider>() {
            @Override
            public void start(DomaDaoProvider component) {
                component.dataSourceComponent = getDependency(DataSourceComponent.class);
            }

            @Override
            public void stop(DomaDaoProvider component) {
                component.dataSourceComponent = null;
            }
        };
    }
}
