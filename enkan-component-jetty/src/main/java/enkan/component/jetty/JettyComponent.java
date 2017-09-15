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
                    OptionMap options = buildOptionMap();
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
}
