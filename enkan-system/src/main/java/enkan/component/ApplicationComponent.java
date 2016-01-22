package enkan.component;

import enkan.application.WebApplication;
import enkan.config.ApplicationConfigurator;
import enkan.config.EnkanSystemFactory;
import enkan.exception.UnrecoverableException;
import enkan.system.inject.ComponentInjector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author kawasima
 */
public class ApplicationComponent extends SystemComponent {
    private WebApplication application;
    private Class<? extends ApplicationConfigurator> configuratorClass;

    public ApplicationComponent(Class<? extends ApplicationConfigurator> configuratorClass) {
        this.configuratorClass = configuratorClass;
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<ApplicationComponent>() {
            @Override
            public void start(ApplicationComponent component) {
                if (component.application == null) {
                    component.application = new WebApplication();
                    try {
                        ApplicationConfigurator configurator = configuratorClass.newInstance();
                        configurator.config(application, new ComponentInjector(getAllDependencies()));
                    } catch (IllegalAccessException | InstantiationException e) {
                        UnrecoverableException.raise(e);
                    }
                }
            }

            @Override
            public void stop(ApplicationComponent component) {
                component.application = null;
            }
        };
    }

    public WebApplication getApplication() {
        return application;
    }

}
