package enkan.throttling;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

/**
 * @author kawasima
 */
public class LimitRate implements Serializable {
    private long max;
    private Duration duration;

    public LimitRate(long max, Duration duration) {
        this.max = max;
        this.duration = duration;
    }

    public long getMax() {
        return this.max;
    }

    public Duration getDuration() {
        return this.duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LimitRate limitRate = (LimitRate) o;
        return max == limitRate.max &&
                Objects.equals(duration, limitRate.duration);
    }

    @Override
    public int hashCode() {

        return Objects.hash(max, duration);
    }

    public String toString() {
        return "LimitRate(max=" + this.getMax() + ", duration=" + this.getDuration() + ")";
    }
}
