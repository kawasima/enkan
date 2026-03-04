package enkan.throttling;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TokenBucketTest {
    private static final SleepStrategy NO_SLEEP = () -> {};

    /** Refill strategy that never refills - tokens only decrease. */
    private static RefillStrategy noRefill() {
        return new RefillStrategy(0, Duration.ofDays(1));
    }

    @Test
    void tryConsumeSucceedsWhileTokensAvailable() {
        TokenBucket bucket = new TokenBucket(3, 3, noRefill(), NO_SLEEP);

        assertThat(bucket.tryConsume(1)).isTrue();
        assertThat(bucket.tryConsume(1)).isTrue();
        assertThat(bucket.tryConsume(1)).isTrue();
        assertThat(bucket.getNumTokens()).isEqualTo(0);
    }

    @Test
    void tryConsumeFailsWhenBucketIsEmpty() {
        TokenBucket bucket = new TokenBucket(3, 0, noRefill(), NO_SLEEP);

        assertThat(bucket.tryConsume(1)).isFalse();
    }

    @Test
    void tryConsumeMultipleTokensAtOnce() {
        TokenBucket bucket = new TokenBucket(5, 5, noRefill(), NO_SLEEP);

        assertThat(bucket.tryConsume(3)).isTrue();
        assertThat(bucket.getNumTokens()).isEqualTo(2);
        assertThat(bucket.tryConsume(3)).isFalse();
        assertThat(bucket.getNumTokens()).isEqualTo(2); // not changed on failure
    }

    @Test
    void refillDoesNotExceedCapacity() {
        TokenBucket bucket = new TokenBucket(3, 0, noRefill(), NO_SLEEP);

        bucket.refill(100);
        assertThat(bucket.getNumTokens()).isEqualTo(3);
    }

    @Test
    void refillWithNegativeIsIgnored() {
        TokenBucket bucket = new TokenBucket(3, 2, noRefill(), NO_SLEEP);

        bucket.refill(-5);
        assertThat(bucket.getNumTokens()).isEqualTo(2);
    }

    @Test
    void tryConsumeZeroThrowsException() {
        TokenBucket bucket = new TokenBucket(3, 3, noRefill(), NO_SLEEP);

        assertThatThrownBy(() -> bucket.tryConsume(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tryConsumeMoreThanCapacityThrowsException() {
        TokenBucket bucket = new TokenBucket(3, 3, noRefill(), NO_SLEEP);

        assertThatThrownBy(() -> bucket.tryConsume(4))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refillStrategyAddsTokensAfterPeriod() throws InterruptedException {
        // capacity=2, starts full, refills 2 tokens every 50ms
        TokenBucket bucket = new TokenBucket(2, 2,
                new RefillStrategy(2, Duration.ofMillis(50)), NO_SLEEP);

        // exhaust the bucket
        assertThat(bucket.tryConsume(1)).isTrue();
        assertThat(bucket.tryConsume(1)).isTrue();
        assertThat(bucket.tryConsume(1)).isFalse();

        // wait for one refill period
        Thread.sleep(60);

        // bucket should be refilled
        assertThat(bucket.tryConsume(1)).isTrue();
        assertThat(bucket.tryConsume(1)).isTrue();
        assertThat(bucket.tryConsume(1)).isFalse();
    }
}
