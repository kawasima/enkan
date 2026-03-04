package enkan.system.command;

import enkan.component.LifecycleManager;
import enkan.component.metrics.MetricsComponent;
import enkan.system.EnkanSystem;
import enkan.system.ReplResponse;
import enkan.system.Transport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsCommandTest {

    /** Captures all sent messages for assertion. */
    static class CapturingTransport implements Transport {
        final List<String> outLines = new ArrayList<>();

        @Override
        public void send(ReplResponse response) {
            String out = response.getOut();
            if (out != null) outLines.add(out);
        }

        @Override
        public String recv(long timeout) {
            return null;
        }
    }

    private MetricsComponent metricsComponent;
    private MetricsCommand command;
    private CapturingTransport transport;

    @BeforeEach
    void setUp() {
        metricsComponent = new MetricsComponent();
        command = new MetricsCommand();
        transport = new CapturingTransport();
    }

    @AfterEach
    void tearDown() {
        if (metricsComponent.getActiveRequests() != null) {
            LifecycleManager.stop(metricsComponent);
        }
    }

    @Test
    void outputsMetricSectionsWhenStarted() {
        LifecycleManager.start(metricsComponent);
        EnkanSystem system = EnkanSystem.of("metrics", metricsComponent);

        command.execute(system, transport);

        assertThat(transport.outLines).anyMatch(l -> l.contains("Active Requests"));
        assertThat(transport.outLines).anyMatch(l -> l.contains("Errors"));
        assertThat(transport.outLines).anyMatch(l -> l.contains("Request Timer"));
    }

    @Test
    void outputsCounterAndTimerValues() {
        LifecycleManager.start(metricsComponent);
        EnkanSystem system = EnkanSystem.of("metrics", metricsComponent);

        command.execute(system, transport);

        assertThat(transport.outLines).anyMatch(l -> l.contains("count"));
        assertThat(transport.outLines).anyMatch(l -> l.contains("mean rate"));
    }

    @Test
    void reportsNotStartedWhenComponentNotStarted() {
        EnkanSystem system = EnkanSystem.of("metrics", metricsComponent);

        command.execute(system, transport);

        assertThat(transport.outLines).anyMatch(l -> l.contains("not started"));
    }

    @Test
    void reportsNotRegisteredWhenNoMetricsComponent() {
        EnkanSystem system = EnkanSystem.of();

        command.execute(system, transport);

        assertThat(transport.outLines).anyMatch(l -> l.contains("not registered"));
    }

    @Test
    void returnsTrue() {
        LifecycleManager.start(metricsComponent);
        EnkanSystem system = EnkanSystem.of("metrics", metricsComponent);

        boolean result = command.execute(system, transport);

        assertThat(result).isTrue();
    }
}
