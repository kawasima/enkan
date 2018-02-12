package enkan.component;

import enkan.Application;
import enkan.MiddlewareChain;
import enkan.config.ApplicationFactory;
import enkan.config.ConfigurationLoader;
import enkan.system.inject.ComponentInjector;

import static enkan.util.ReflectionUtils.*;

/**
 * Provides an application.
 *
 * @author kawasima
 */
public class ApplicationComponent extends SystemComponent {
    private Application application;
    private ConfigurationLoader loader;
    private final String factoryClassName;
    private ClassLoader originalLoader;

    public ApplicationComponent(String className) {
        this.factoryClassName = className;
    }

    @Override
    protected ComponentLifecycle<ApplicationComponent> lifecycle() {
        return new ComponentLifecycle<ApplicationComponent>() {
            @Override
            public void start(ApplicationComponent component) {
                if (component.application == null) {
                    component.application = tryReflection(() -> {
                        component.loader = new ConfigurationLoader(getClass().getClassLoader());
                        component.originalLoader = Thread.currentThread().getContextClassLoader();
                        Thread.currentThread().setContextClassLoader(loader);
                        Class<? extends ApplicationFactory> factoryClass =
                                (Class<? extends ApplicationFactory>) loader.loadClass(factoryClassName);
                        ComponentInjector injector = new ComponentInjector(getAllDependencies());
                        ApplicationFactory factory = factoryClass.getConstructor().newInstance();
                        Application<?, ?> app = factory.create(injector);
                        app.getMiddlewareStack().stream()
                                .map(MiddlewareChain::getMiddleware)
                                .forEach(injector::inject);
                        app.validate();
                        return app;
                    });
                }
            }

            @Override
            public void stop(ApplicationComponent component) {
                component.application = null;
                component.loader = null;
                if (originalLoader != null) {
                    Thread.currentThread().setContextClassLoader(originalLoader);
                }
            }
        };
    }

    public Application getApplication() {
        return application;
    }

    public ConfigurationLoader getLoader() {
        return loader;
    }

    public String getFactoryClassName() {
        return factoryClassName;
    }

    @Override
    public String toString() {
        return "#ApplicationComponent {\n"
                + "  \"application\": \"" + application + "\",\n"
                + "  \"factoryClassName\": \"" + factoryClassName + "\",\n"
                + "  \"dependencies\": " + dependenciesToString()
                + "\n}";

    }
}
