package enkan.component;

import enkan.adapter.JettyAdapter;
import enkan.collection.OptionMap;
import enkan.exception.FalteringEnvironmentException;
import enkan.exception.UnrecoverableException;
import org.eclipse.jetty.server.Server;

/**
 * @author kawasima
 */
public class JettyComponent extends SystemComponent {
    private OptionMap options;
    private Server server;

    public JettyComponent(OptionMap options) {
        this.options = options;
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<JettyComponent>() {
            @Override
            public void start(JettyComponent component) {
                ApplicationComponent app = getDependency(ApplicationComponent.class);
                if (server == null) {
                    options.put("join?", false);
                    server = new JettyAdapter().runJetty(app.getApplication(), options);
                }
            }

            @Override
            public void stop(JettyComponent component) {
                if (server != null) {
                    try {
                        server.stop();
                        server.join();
                    } catch (Exception e) {
                        throw FalteringEnvironmentException.create(e);
                    } finally {
                        server = null;
                    }
                }

            }
        };
    }
}
