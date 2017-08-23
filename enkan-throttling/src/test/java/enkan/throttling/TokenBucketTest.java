package enkan.throttling;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TokenBucketTest {
    @Test
    public void test() {
        TokenBucket bucket = new TokenBucket(3, 3, new RefillStrategy(6, 1, TimeUnit.MINUTES), new SleepStrategy() {
            @Override
            public void sleep() {

            }
        });

        bucket.consume();
        bucket.consume();
        bucket.consume();
        bucket.consume();
    }
}
