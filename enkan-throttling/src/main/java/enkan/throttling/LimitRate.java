package enkan.throttling;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.Duration;

/**
 * @author kawasima
 */
@Data
@AllArgsConstructor
public class LimitRate implements Serializable {
    private long max;
    private Duration duration;
}
