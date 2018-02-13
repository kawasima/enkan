package enkan.component.jetty;

import enkan.adapter.JettyAdapter;
import enkan.application.WebApplication;
import enkan.collection.OptionMap;
import enkan.component.ApplicationComponent;
import enkan.component.ComponentLifecycle;
import enkan.component.WebServerComponent;
import enkan.exception.FalteringEnvironmentException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;

import java.util.function.BiFunction;

/**
 * @author kawasima
 */
public class JettyComponent extends WebServerComponent<JettyComponent> {
    private Server server;
    private BiFunction<Server, OptionMap, Connector> serverConnectorFactory;

    @Override
    protected ComponentLifecycle<JettyComponent> lifecycle() {
        return new ComponentLifecycle<JettyComponent>() {
            @Override
            public void start(JettyComponent component) {
                ApplicationComponent app = getDependency(ApplicationComponent.class);
                if (server == null) {
                    OptionMap options = buildOptionMap();
                    if (serverConnectorFactory != null) options.put("serverConnectorFactory", serverConnectorFactory);
                    options.put("join?", false);
                    server = new JettyAdapter().runJetty((WebApplication) app.getApplication(), options);
                }
            }

            @Override
            public void stop(JettyComponent component) {
                if (server != null) {
                    try {
                        server.stop();
                        server.join();
                    } catch (Exception ex) {
                        throw new FalteringEnvironmentException(ex);
                    } finally {
                        server = null;
                    }
                }

            }
        };
    }

    /**
     * Set a factory of jetty connector.
     *
     * @param  factory A factory of Jetty connector
     */
    public void setServerConnectorFactory(BiFunction<Server, OptionMap, Connector> factory) {
        this.serverConnectorFactory = factory;
    }
}
