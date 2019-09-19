package enkan.component;

import enkan.Application;
import enkan.MiddlewareChain;
import enkan.config.ApplicationFactory;
import enkan.config.ConfigurationLoader;
import enkan.system.inject.ComponentInjector;

import java.util.function.Function;

import static enkan.util.ReflectionUtils.*;

/**
 * Provides an application.
 *
 * @author kawasima
 */
public class ApplicationComponent<AREQ, ARES> extends SystemComponent {
    /** An application instance*/
    private Application<AREQ, ARES> application;

    /** An application loader */
    private ConfigurationLoader loader;

    /** A name of the application factory class */
    private final String factoryClassName;

    private ClassLoader originalLoader;

    /** A customizer for an application */
    private Function<Application<AREQ,ARES>, Application<AREQ,ARES>> applicationCustomizer;

    public ApplicationComponent(String className) {
        this.factoryClassName = className;
    }

    @Override
    protected ComponentLifecycle<ApplicationComponent> lifecycle() {
        return new ComponentLifecycle<ApplicationComponent>() {
            @SuppressWarnings("unchecked")
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
                        Application<AREQ, ARES> app = factory.create(injector);
                        app.getMiddlewareStack().stream()
                                .map(MiddlewareChain::getMiddleware)
                                .forEach(injector::inject);

                        if (applicationCustomizer != null) {
                            app = applicationCustomizer.apply(app);
                        }
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

    public void setApplicationCustomizer(Function<Application<AREQ,ARES>, Application<AREQ,ARES>> applicationCustomizer) {
        this.applicationCustomizer = applicationCustomizer;
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
