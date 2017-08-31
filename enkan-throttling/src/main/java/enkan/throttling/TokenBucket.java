package enkan.throttling;

import java.util.concurrent.TimeUnit;

/**
 * @author kawasima
 */
public class TokenBucket {
    private final long capacity;
    private final RefillStrategy refillStrategy;
    private final SleepStrategy sleepStrategy;
    private long size;

    public TokenBucket(long capacity, long initialTokens, RefillStrategy refillStrategy, SleepStrategy sleepStrategy) {
        this.capacity = capacity;
        this.refillStrategy = refillStrategy;
        this.sleepStrategy = sleepStrategy;
        this.size = initialTokens;
    }

    public long getCapacity()
    {
        return capacity;
    }

    public synchronized long getNumTokens()
    {
        // Give the refill strategy a chance to add tokens if it needs to so that we have an accurate
        // count.
        refill(refillStrategy.refill());

        return size;
    }

    /**
     * Returns the amount of time in the specified time unit until the next group of tokens can be added to the token
     * bucket.
     *
     * @param unit The time unit to express the return value in.
     * @return The amount of time until the next group of tokens can be added to the token bucket.
     */
    public long getDurationUntilNextRefill(TimeUnit unit) throws UnsupportedOperationException
    {
        return refillStrategy.getDurationUntilNextRefill(unit);
    }

    /**
     * Attempt to consume a single token from the bucket.  If it was consumed then {@code true} is returned, otherwise
     * {@code false} is returned.
     *
     * @return {@code true} if a token was consumed, {@code false} otherwise.
     */
    public boolean tryConsume()
    {
        return tryConsume(1);
    }

    /**
     * Attempt to consume a specified number of tokens from the bucket.  If the tokens were consumed then {@code true}
     * is returned, otherwise {@code false} is returned.
     *
     * @param numTokens The number of tokens to consume from the bucket, must be a positive number.
     * @return {@code true} if the tokens were consumed, {@code false} otherwise.
     */
    public synchronized boolean tryConsume(long numTokens)
    {
        if (numTokens <= 0)
            throw new IllegalArgumentException("Number of tokens to consume must be positive");
        if(numTokens > capacity)
            throw new IllegalArgumentException("Number of tokens to consume must be less than the capacity of the bucket.");

        refill(refillStrategy.refill());

        // Now try to consume some tokens
        if (numTokens <= size) {
            size -= numTokens;
            return true;
        }

        return false;
    }

    /**
     * Consume a single token from the bucket.  If no token is currently available then this method will block until a
     * token becomes available.
     */
    public void consume()
    {
        consume(1);
    }

    /**
     * Consumes multiple tokens from the bucket.  If enough tokens are not currently available then this method will block
     * until
     *
     * @param numTokens The number of tokens to consume from teh bucket, must be a positive number.
     */
    public void consume(long numTokens)
    {
        while (true) {
            if (tryConsume(numTokens)) {
                break;
            }

            sleepStrategy.sleep();
        }
    }

    /**
     * Refills the bucket with the specified number of tokens.  If the bucket is currently full or near capacity then
     * fewer than {@code numTokens} may be added.
     *
     * @param numTokens The number of tokens to add to the bucket.
     */
    public synchronized void refill(long numTokens)
    {
        long newTokens = Math.min(capacity, Math.max(0, numTokens));
        size = Math.max(0, Math.min(size + newTokens, capacity));
    }
}
