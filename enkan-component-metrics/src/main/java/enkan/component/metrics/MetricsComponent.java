package enkan.component.metrics;

import com.codahale.metrics.*;
import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;

import java.util.Collections;
import java.util.SortedSet;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Metrics component.
 *
 * @author kawasima
 */
public class MetricsComponent extends SystemComponent<MetricsComponent> {
    private String metricName = "enkan";

    private Meter timeoutsMeter;
    private Meter errorsMeter;
    private Counter activeRequests;
    private Timer requestTimer;

    private final JmxReporter reporter;
    private final MetricRegistry metricRegistry;

    public MetricsComponent() {
        metricRegistry = new MetricRegistry();
        reporter = JmxReporter.forRegistry(metricRegistry).build();
    }

    @Override
    protected ComponentLifecycle<MetricsComponent> lifecycle() {
        return new ComponentLifecycle<MetricsComponent>() {
            @Override
            public void start(MetricsComponent component) {
                component.timeoutsMeter = metricRegistry.meter(name(metricName, "timeouts"));
                component.errorsMeter = metricRegistry.meter(name(metricName, "errors"));
                component.activeRequests = metricRegistry.counter(name(metricName, "activeRequests"));
                component.requestTimer = metricRegistry.timer(name(metricName, "requestTimer"));
                reporter.start();
            }

            @Override
            public void stop(MetricsComponent component) {
                SortedSet<String> names = Collections.unmodifiableSortedSet(metricRegistry.getNames());
                names.forEach(metricRegistry::remove);

                component.timeoutsMeter = null;
                component.errorsMeter = null;
                component.activeRequests = null;
                component.requestTimer = null;

                reporter.stop();
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

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
}
