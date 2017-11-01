package enkan.throttling;

import org.junit.jupiter.api.Test;

import java.time.Duration;

public class TokenBucketTest {
    @Test
    public void test() {
        TokenBucket bucket = new TokenBucket(3, 3, new RefillStrategy(6, Duration.ofSeconds(1)), () -> {

        });
        bucket.consume();
        bucket.consume();
        bucket.consume();
        bucket.consume();
    }
}
