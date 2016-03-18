package enkan.system.command;

import com.codahale.metrics.*;
import enkan.component.MetricsComponent;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.ReplResponse;
import enkan.system.Transport;
import enkan.system.repl.SystemCommandRegister;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
                String.format(Locale.US, "             count = %d", meter.getCount())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "         mean rate = %2.2f events/s", meter.getMeanRate())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "     1-minute rate = %2.2f events/s", meter.getOneMinuteRate())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "     5-minute rate = %2.2f events/s", meter.getFiveMinuteRate())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "    15-minute rate = %2.2f events/s", meter.getFifteenMinuteRate())));
    }


    private void printCounter(Transport t, Counter counter) {
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "             count = %d", counter.getCount())));
    }

    private void printGauge(Transport t, Gauge guage) {
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "             value = %d", guage.getValue())
        ));
    }

    private double convertDuration(double duration) {
        return duration * (1.0 / TimeUnit.SECONDS.toNanos(1));

    }
    private void printTimer(Transport t, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "             count = %d", timer.getCount())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "         mean rate = %2.2f calls/sec", timer.getMeanRate())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "     1-minute rate = %2.2f calls/sec", timer.getMeanRate())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "     5-minute rate = %2.2f calls/sec", timer.getFiveMinuteRate())));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "    15-minute rate = %2.2f calls/sec", timer.getFifteenMinuteRate())));

        t.send(ReplResponse.withOut(
                String.format(Locale.US, "               min = %2.2f sec", convertDuration(snapshot.getMin()))));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "               max = %2.2f sec", convertDuration(snapshot.getMax()))));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "              mean = %2.2f sec", convertDuration(snapshot.getMean()))));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "            stddev = %2.2f sec", convertDuration(snapshot.getStdDev()))));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "            median = %2.2f sec", convertDuration(snapshot.getMedian()))));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "              75%% <= %2.2f sec", convertDuration(snapshot.get75thPercentile()))));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "              95%% <= %2.2f sec", convertDuration(snapshot.get95thPercentile()))));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "              98%% <= %2.2f sec", convertDuration(snapshot.get98thPercentile()))));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "              99%% <= %2.2f sec", convertDuration(snapshot.get99thPercentile()))));
        t.send(ReplResponse.withOut(
                String.format(Locale.US, "            99.9%% <= %2.2f sec", convertDuration(snapshot.get999thPercentile()))));

    }
    @Override
    public void register(Repl repl) {
        repl.registerCommand("metrics", ((system, transport, args) -> {
            findMetrics(system).ifPresent(metrics -> {
                transport.send(ReplResponse.withOut("-- Active Requests ----------------------------------"));
                printCounter(transport, metrics.getActiveRequests());
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
