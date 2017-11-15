package enkan.throttling;

import enkan.data.HttpRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author kawasima
 */
public class Throttle {
    final private Function<HttpRequest, Object> discriminateFn;
    final private String name;
    final private LimitRate limitRate;
    final private Map<Object, TokenBucket> buckets;


    public Throttle(String name, LimitRate limitRate, Function<HttpRequest, Object> discriminateFn) {
        this.name = name;
        this.limitRate = limitRate;
        this.discriminateFn = discriminateFn;
        this.buckets = new ConcurrentHashMap<>();
    }

    public boolean apply(HttpRequest request) {
        Object discriminator = discriminateFn.apply(request);
        if (discriminator == null) return false;

        TokenBucket bucket = buckets.computeIfAbsent(discriminator, key ->
                new TokenBucket(limitRate.getMax(), limitRate.getMax(),
                        new RefillStrategy(limitRate.getMax(), limitRate.getDuration()),
                        () -> {
                            boolean interrupted = false;
                            try {
                                TimeUnit.NANOSECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                interrupted = true;
                            } finally {
                                if (interrupted) Thread.currentThread().interrupt();
                            }
                        })
        );
        if (bucket.tryConsume(1)) {
            bucket.consume();
            return false;
        } else {
            return true;
        }
    }
}
