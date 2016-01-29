package enkan.component;

import enkan.Application;
import enkan.config.ApplicationFactory;
import enkan.config.ConfigurationLoader;
import enkan.system.inject.ComponentInjector;

import static enkan.util.ReflectionUtils.tryReflection;

/**
 * @author kawasima
 */
public class ApplicationComponent extends SystemComponent {
    private Application application;
    private ConfigurationLoader loader;
    private String factoryClassName;

    public ApplicationComponent(String className) {
        this.factoryClassName = className;
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<ApplicationComponent>() {
            @Override
            public void start(ApplicationComponent component) {
                if (component.application == null) {
                    component.application = tryReflection(() -> {
                        loader = new ConfigurationLoader(getClass().getClassLoader());
                        Class<? extends ApplicationFactory> factoryClass =
                                (Class<? extends ApplicationFactory>) loader.loadClass(factoryClassName);
                        ApplicationFactory factory = factoryClass.newInstance();
                        return factory.create(new ComponentInjector(getAllDependencies()));
                    });
                    component.application.validate();
                }
            }

            @Override
            public void stop(ApplicationComponent component) {
                component.application = null;
                loader = null;
            }
        };
    }

    public Application getApplication() {
        return application;
    }

}
