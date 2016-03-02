package enkan.system.command;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import enkan.component.MetricsComponent;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.ReplResponse;
import enkan.system.Transport;
import enkan.system.repl.SystemCommandRegister;

import java.util.Locale;
import java.util.Optional;

/**
 * @author kawasima
 */
public class MetricsCommandRegister implements SystemCommandRegister {
    protected Optional<MetricsComponent> findMetrics(EnkanSystem system) {
        return system.getAllComponents().stream()
                .filter(c -> c instanceof MetricsComponent)
                .map(c -> MetricsComponent.class.cast(c))
                .findFirst();
    }

    private void printMeter(Transport t, Meter meter) {
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "             count = %d%n", meter.getCount())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "         mean rate = %2.2f events/s%n", meter.getMeanRate())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "     1-minute rate = %2.2f events/s%n", meter.getOneMinuteRate())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "     5-minute rate = %2.2f events/s%n", meter.getFiveMinuteRate())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "    15-minute rate = %2.2f events/s%n", meter.getFifteenMinuteRate())));
    }

    /*

        private void printCounter(Map.Entry<MetricName, Counter> entry) {
            output.printf(locale, "             count = %d%n", entry.getValue().getCount());
        }
        private void printGauge(Map.Entry<MetricName, Gauge> entry) {
            output.printf(locale, "             value = %s%n", entry.getValue().getValue());
        }

        private void printHistogram(Histogram histogram) {
            output.printf(locale, "             count = %d%n", histogram.getCount());
            Snapshot snapshot = histogram.getSnapshot();
            output.printf(locale, "               min = %d%n", snapshot.getMin());
            output.printf(locale, "               max = %d%n", snapshot.getMax());
            output.printf(locale, "              mean = %2.2f%n", snapshot.getMean());
            output.printf(locale, "            stddev = %2.2f%n", snapshot.getStdDev());
            output.printf(locale, "            median = %2.2f%n", snapshot.getMedian());
            output.printf(locale, "              75%% <= %2.2f%n", snapshot.get75thPercentile());
            output.printf(locale, "              95%% <= %2.2f%n", snapshot.get95thPercentile());
            output.printf(locale, "              98%% <= %2.2f%n", snapshot.get98thPercentile());
            output.printf(locale, "              99%% <= %2.2f%n", snapshot.get99thPercentile());
            output.printf(locale, "            99.9%% <= %2.2f%n", snapshot.get999thPercentile());
        }
    */
    private void printTimer(Transport t, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "             count = %d%n", timer.getCount())));
    }
    @Override
    public void register(Repl repl) {
        repl.registerCommand("metrics", ((system, transport, args) -> {
            findMetrics(system).ifPresent(metrics -> {
                transport.send(ReplResponse.withOut("-- Errors ------------------------------------"));
                printMeter(transport, metrics.getErrors());
                transport.send(ReplResponse.withOut("-- Request Timer -----------------------------"));
                printTimer(transport, metrics.getRequestTimer());
                transport.sendOut("", ReplResponse.ResponseStatus.DONE);
            });
            return true;
        }));
    }
}
