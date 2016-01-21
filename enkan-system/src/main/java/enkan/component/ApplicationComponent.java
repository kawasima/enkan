package enkan.component;

import enkan.application.WebApplication;
import enkan.config.ApplicationConfigurator;
import enkan.exception.UnrecoverableException;
import enkan.system.inject.ComponentInjector;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author kawasima
 */
public class ApplicationComponent extends SystemComponent {
    private WebApplication application;
    private String configuratorPath;

    public ApplicationComponent(String configuratorPath) {
        this.configuratorPath = configuratorPath;
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<ApplicationComponent>() {
            @Override
            public void start(ApplicationComponent component) {
                if (component.application == null) {
                    component.application = new WebApplication();
                    try {
                        ApplicationConfigurator configurator = (ApplicationConfigurator) new ApplicationClassLoader().loadClass(configuratorPath).newInstance();
                        configurator.config(application, new ComponentInjector(getAllDependencies()));
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
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

    static class ApplicationClassLoader extends URLClassLoader {
        static URL[] EMPTY_URLS = new URL[]{};

        ApplicationClassLoader() {
            super(EMPTY_URLS, Thread.currentThread().getContextClassLoader());
        }

        public Class defineClass(String name, byte[] bytes, Object srcForm) {
            return defineClass(name, bytes, 0, bytes.length);
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class c = findLoadedClass(name);
            if (c == null) {
                c = super.loadClass(name, false);
            }
            if (resolve)
                resolveClass(c);
            return c;
        }

        @Override
        protected void addURL(URL url) {
            super.addURL(url);
        }
    }
}
