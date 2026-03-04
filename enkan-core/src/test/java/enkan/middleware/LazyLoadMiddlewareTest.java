package enkan.middleware;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.util.Predicates;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class LazyLoadMiddlewareTest {

    /** Simple middleware that counts how many times it was instantiated. */
    public static class CountingMiddleware implements enkan.Middleware<String, String, String, String> {
        static final AtomicInteger instanceCount = new AtomicInteger(0);

        public CountingMiddleware() {
            instanceCount.incrementAndGet();
        }

        @Override
        public <NNREQ, NNRES> String handle(String request, MiddlewareChain<String, String, NNREQ, NNRES> next) {
            return "handled:" + request;
        }
    }

    private static MiddlewareChain<String, String, ?, ?> terminalChain() {
        return new DefaultMiddlewareChain<>(Predicates.any(), "terminal",
                (Endpoint<String, String>) req -> req);
    }

    @Test
    void loadsMiddlewareOnFirstCall() {
        CountingMiddleware.instanceCount.set(0);

        LazyLoadMiddleware<String, String, String, String> lazy =
                new LazyLoadMiddleware<>(CountingMiddleware.class.getName());

        assertThat(CountingMiddleware.instanceCount.get())
                .as("middleware must not be instantiated before first call")
                .isEqualTo(0);

        String result = lazy.handle("hello", terminalChain());
        assertThat(result).isEqualTo("handled:hello");
        assertThat(CountingMiddleware.instanceCount.get()).isEqualTo(1);
    }

    @Test
    void instantiatesOnlyOnceUnderConcurrency() throws InterruptedException {
        CountingMiddleware.instanceCount.set(0);

        LazyLoadMiddleware<String, String, String, String> lazy =
                new LazyLoadMiddleware<>(CountingMiddleware.class.getName());

        int threadCount = 20;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                try {
                    start.await();
                    lazy.handle("x", terminalChain());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
            t.start();
        }

        start.countDown();
        done.await();

        assertThat(CountingMiddleware.instanceCount.get())
                .as("middleware must be instantiated exactly once even under concurrent access")
                .isEqualTo(1);
    }

    @Test
    void throwsWhenClassNotFound() {
        LazyLoadMiddleware<String, String, String, String> lazy =
                new LazyLoadMiddleware<>("no.such.MiddlewareClass");

        assertThatThrownBy(() -> lazy.handle("x", terminalChain()))
                .isInstanceOf(enkan.exception.MisconfigurationException.class)
                .hasFieldOrPropertyWithValue("code", "core.CLASS_NOT_FOUND");
    }
}
