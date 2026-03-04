package enkan.component.undertow;

import enkan.adapter.UndertowAdapter;
import enkan.application.WebApplication;
import enkan.collection.OptionMap;
import enkan.component.ApplicationComponent;
import enkan.component.ComponentLifecycle;
import enkan.component.HealthCheckable;
import enkan.component.HealthStatus;
import enkan.component.WebServerComponent;
import enkan.exception.MisconfigurationException;
import io.undertow.Undertow;

/**
 * @author kawasima
 */
public class UndertowComponent extends WebServerComponent<UndertowComponent> implements HealthCheckable {
    private Undertow server;

    @Override
    protected ComponentLifecycle<UndertowComponent> lifecycle() {
        return new ComponentLifecycle<>() {
            @Override
            public void start(UndertowComponent component) {
                ApplicationComponent<?, ?> app = getDependency(ApplicationComponent.class);
                if (server == null) {
                    OptionMap options = buildOptionMap();
                    if (!(app.getApplication() instanceof WebApplication webApp)) {
                        throw new MisconfigurationException("web.APPLICATION_NOT_WEB");
                    }
                    server = new UndertowAdapter().runUndertow(webApp, options);
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

    @Override
    public HealthStatus health() {
        return server != null ? HealthStatus.UP : HealthStatus.DOWN;
    }

    @Override
    public String toString() {
        return "#UndertowComponent {\n"
                + "  \"host\": \"" + getHost() + "\",\n"
                + "  \"port\": \"" + getPort() + "\",\n"
                + "  \"dependencies\": \"" + dependenciesToString() + ",\n"
                + "}";
    }
}
