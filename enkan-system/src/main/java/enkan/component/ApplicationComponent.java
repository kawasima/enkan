package enkan.component;

import enkan.application.WebApplication;
import enkan.config.ApplicationConfigurator;
import enkan.config.ConfigurationLoader;
import enkan.exception.MisconfigurationException;
import enkan.exception.UnreachableException;
import enkan.exception.UnrecoverableException;
import enkan.system.inject.ComponentInjector;

/**
 * @author kawasima
 */
public class ApplicationComponent extends SystemComponent {
    private WebApplication application;
    private ConfigurationLoader loader;
    private String configuratorClassName;

    public ApplicationComponent(String className) {
        this.configuratorClassName = className;
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<ApplicationComponent>() {
            @Override
            public void start(ApplicationComponent component) {
                if (component.application == null) {
                    component.application = new WebApplication();
                    Class<? extends ApplicationConfigurator> configuratorClass = null;
                    try {
                        loader = new ConfigurationLoader(getClass().getClassLoader());
                        configuratorClass =
                                (Class<? extends ApplicationConfigurator>) loader.loadClass(configuratorClassName);
                        ApplicationConfigurator configurator = configuratorClass.newInstance();
                        configurator.config(application, new ComponentInjector(getAllDependencies()));
                    } catch (ClassNotFoundException ex) {
                        MisconfigurationException.raise("CLASS_NOT_FOUND", "ApplicationConfigurator", configuratorClassName);
                    } catch (ClassCastException ex) {
                        MisconfigurationException.raise("CLASS_NOT_FOUND", "ApplicationConfigurator", configuratorClassName);
                    } catch (IllegalAccessException ex) {
                        if (configuratorClass != null) {
                            MisconfigurationException.raise("ILLEGAL_ACCESS", "ApplicationConfigurator", configuratorClassName);
                        } else {
                            UnreachableException.create();
                        }
                    } catch (InstantiationException ex) {
                        MisconfigurationException.raise("INSTANTIATION", "ApplicationConfigurator", configuratorClassName);
                    }
                }
            }

            @Override
            public void stop(ApplicationComponent component) {
                component.application = null;
                loader = null;
            }
        };
    }

    public WebApplication getApplication() {
        return application;
    }

}
