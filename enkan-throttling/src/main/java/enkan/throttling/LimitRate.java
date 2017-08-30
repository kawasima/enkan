package enkan.throttling;

import java.time.Duration;

/**
 * @author kawasima
 */
public class RateLimiter {
    private long max;
    private Duration duration;

    public RateLimiter(long max, Duration duration) {
        this.max = max;
        this.duration = duration;
    }

    public long
}
