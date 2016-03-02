package enkan.component;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author kawasima
 */
public class MetricsComponent extends SystemComponent {
    MetricRegistry metricRegistry;
    private Meter timeoutsMeter;
    private Meter errorsMeter;
    private Counter activeRequests;
    private Timer requestTimer;

    @Override
    protected ComponentLifecycle<MetricsComponent> lifecycle() {
        return new ComponentLifecycle<MetricsComponent>() {
            @Override
            public void start(MetricsComponent component) {
                String metricName = "enkan";
                metricRegistry = new MetricRegistry();
                component.timeoutsMeter = metricRegistry.meter(name(metricName, "timeouts"));
                component.errorsMeter = metricRegistry.meter(name(metricName, "errors"));
                component.activeRequests = metricRegistry.counter(name(metricName, "activeRequests"));
                component.requestTimer = metricRegistry.timer(name(metricName, "requestTimer"));

            }

            @Override
            public void stop(MetricsComponent component) {
                metricRegistry = null;
            }
        };
    }

    public Meter getTimeouts() {
        return timeoutsMeter;
    }

    public Timer getRequestTimer() {
        return requestTimer;
    }

    public Meter getErrors() {
        return errorsMeter;
    }

    public Counter getActiveRequests() {
        return activeRequests;
    }
}
