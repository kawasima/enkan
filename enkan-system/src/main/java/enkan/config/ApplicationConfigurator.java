package enkan.config;

import enkan.Application;
import enkan.system.inject.ComponentInjector;

/**
 * @author kawasima
 */
public interface ApplicationConfigurator {
    void config(Application application, ComponentInjector injector);
}
