package enkan.component.undertow;

import enkan.adapter.UndertowAdapter;
import enkan.application.WebApplication;
import enkan.collection.OptionMap;
import enkan.component.ApplicationComponent;
import enkan.component.ComponentLifecycle;
import enkan.component.WebServerComponent;
import io.undertow.Undertow;

/**
 * @author kawasima
 */
public class UndertowComponent extends WebServerComponent {
    Integer port;
    private Undertow server;

    @Override
    protected ComponentLifecycle<UndertowComponent> lifecycle() {
        return new ComponentLifecycle<UndertowComponent>() {
            @Override
            public void start(UndertowComponent component) {
                ApplicationComponent app = getDependency(ApplicationComponent.class);
                if (server == null) {
                    OptionMap options = OptionMap.of("join?", false);
                    if (port != null) options.put("port", port);
                    server = new UndertowAdapter().runUndertow((WebApplication) app.getApplication(), options);
                }

            }

            @Override
            public void stop(UndertowComponent component) {
                if (server != null) {
                    server.stop();
                    server = null;
                }
            }
        };
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getPort() {
        return port;
    }
}
