package enkan.config;

import enkan.Application;
import enkan.system.inject.ComponentInjector;

/**
 * @author kawasima
 */
public interface ApplicationFactory {
    Application create(ComponentInjector injector);
}
