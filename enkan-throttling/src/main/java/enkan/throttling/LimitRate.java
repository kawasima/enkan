package enkan.throttling;

import java.io.Serializable;
import java.time.Duration;

/**
 * @author kawasima
 */
public record LimitRate(long max, Duration duration) implements Serializable {

    public String toString() {
        return "LimitRate(max=" + this.max() + ", duration=" + this.duration() + ")";
    }
}
