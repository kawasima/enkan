package enkan.throttling;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author kawasima
 */
public class RefillStrategy {
    private final long numTokensPerPeriod;
    private final long periodDurationInNanos;
    private long lastRefillTime;
    private long nextRefillTime;

    public RefillStrategy(long numTokensPerPeriod, Duration duration) {
        this.numTokensPerPeriod = numTokensPerPeriod;
        this.periodDurationInNanos = duration.toNanos();
        this.lastRefillTime = -periodDurationInNanos;
        this.nextRefillTime = -periodDurationInNanos;
    }

    public synchronized long refill() {
        long now = System.nanoTime();
        if (now < nextRefillTime) {
            return 0;
        }

        // We now know that we need to refill the bucket with some tokens, the question is how many.  We need to count how
        // many periods worth of tokens we've missed.
        long numPeriods = Math.max(0, (now - lastRefillTime) / periodDurationInNanos);

        // Move the last refill time forward by this many periods.
        lastRefillTime += numPeriods * periodDurationInNanos;

        // ...and we'll refill again one period after the last time we refilled.
        nextRefillTime = lastRefillTime + periodDurationInNanos;

        return numPeriods * numTokensPerPeriod;
    }

    public long getDurationUntilNextRefill(TimeUnit unit) {
        long now = System.nanoTime();
        return unit.convert(Math.max(0, nextRefillTime - now), TimeUnit.NANOSECONDS);
    }
}
