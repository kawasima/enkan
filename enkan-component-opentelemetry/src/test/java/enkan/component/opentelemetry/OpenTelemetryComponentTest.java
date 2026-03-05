package enkan.component.opentelemetry;

import enkan.component.LifecycleManager;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenTelemetryComponentTest {

    @Test
    void startCreatesTracer() {
        var component = new OpenTelemetryComponent();
        LifecycleManager.start(component);
        assertThat(component.getTracer()).isNotNull();
        LifecycleManager.stop(component);
    }

    @Test
    void stopNullsTracer() {
        var component = new OpenTelemetryComponent();
        LifecycleManager.start(component);
        LifecycleManager.stop(component);
        assertThat(component.getTracer()).isNull();
    }

    @Test
    void customOpenTelemetryInstanceIsUsed() {
        var noop = OpenTelemetry.noop();
        var component = new OpenTelemetryComponent(noop);
        LifecycleManager.start(component);
        assertThat(component.getOpenTelemetry()).isSameAs(noop);
        LifecycleManager.stop(component);
    }
}
