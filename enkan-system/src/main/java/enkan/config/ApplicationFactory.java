package enkan.config;

import enkan.Application;
import enkan.system.inject.ComponentInjector;

/**
 * A factory interface for instantiation of application.
 *
 * @author kawasima
 */
public interface ApplicationFactory {
    Application create(ComponentInjector injector);
}
