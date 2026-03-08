package enkan.system.command;

import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import enkan.system.EnkanSystem;
import enkan.system.ReplResponse;
import enkan.system.Transport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatusCommandTest {

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

    private static class NoopComponent extends SystemComponent<NoopComponent> {
        @Override
        protected ComponentLifecycle<NoopComponent> lifecycle() {
            return new ComponentLifecycle<>() {
                @Override public void start(NoopComponent c) {}
                @Override public void stop(NoopComponent c) {}
            };
        }
    }

    private StatusCommand command;
    private CapturingTransport transport;

    @BeforeEach
    void setUp() {
        command = new StatusCommand();
        transport = new CapturingTransport();
    }

    @Test
    void reportsStoppedBeforeStart() {
        EnkanSystem system = EnkanSystem.of("c", new NoopComponent());

        command.execute(system, transport);

        assertThat(transport.outLines).anyMatch(l -> l.contains("stopped"));
    }

    @Test
    void reportsStartedAfterStart() {
        NoopComponent component = new NoopComponent();
        EnkanSystem system = EnkanSystem.of("c", component);
        system.start();

        command.execute(system, transport);

        assertThat(transport.outLines).anyMatch(l -> l.contains("started"));

        system.stop();
    }

    @Test
    void reportsStoppedAfterStop() {
        NoopComponent component = new NoopComponent();
        EnkanSystem system = EnkanSystem.of("c", component);
        system.start();
        system.stop();

        command.execute(system, transport);

        assertThat(transport.outLines).anyMatch(l -> l.contains("stopped"));
    }

    @Test
    void returnsFalse() {
        EnkanSystem system = EnkanSystem.of();

        boolean result = command.execute(system, transport);

        assertThat(result).isFalse();
    }
}
