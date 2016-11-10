package enkan.component.jetty;

import enkan.adapter.JettyAdapter;
import enkan.application.WebApplication;
import enkan.collection.OptionMap;
import enkan.component.ApplicationComponent;
import enkan.component.ComponentLifecycle;
import enkan.component.WebServerComponent;
import enkan.exception.FalteringEnvironmentException;
import org.eclipse.jetty.server.Server;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

/**
 * @author kawasima
 */
public class JettyComponent extends WebServerComponent {
    @DecimalMax("65535")
    @DecimalMin("1")
    private Integer port;

    private String host;

    private Server server;

    public JettyComponent() {

    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<JettyComponent>() {
            @Override
            public void start(JettyComponent component) {
                ApplicationComponent app = getDependency(ApplicationComponent.class);
                if (server == null) {
                    OptionMap options = OptionMap.of("join?", false);
                    if (port != null) options.put("port", port);
                    if (host != null) options.put("host", host);
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

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }
}
