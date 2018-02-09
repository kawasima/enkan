package enkan.middleware.throttling;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.AnyPredicate;
import enkan.throttling.LimitRate;
import enkan.throttling.Throttle;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static enkan.util.BeanBuilder.*;
import static org.assertj.core.api.Assertions.*;

public class ThrottlingMiddlewareTest {
    @Test
    public void sameIP() throws InterruptedException {
        List<Throttle> throttles = Collections.singletonList(
                new Throttle("IP", new LimitRate(1, Duration.ofMillis(500L)), HttpRequest::getRemoteAddr)
        );

        ThrottlingMiddleware middleware = builder(new ThrottlingMiddleware())
                .set(ThrottlingMiddleware::setThrottles, throttles)
                .build();

        DefaultHttpRequest req = new DefaultHttpRequest();
        req.setRemoteAddr("127.0.0.1");
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain(new AnyPredicate(), null,
                (Endpoint<HttpRequest, HttpResponse>) request -> HttpResponse.of(""));
        final AtomicInteger count200 = new AtomicInteger(0);
        ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
        service.scheduleAtFixedRate(() -> {
            HttpResponse res = middleware.handle(req, chain);
            if (res.getStatus() == 200) {
                count200.addAndGet(1);
            }
        }, 0, 250, TimeUnit.MILLISECONDS);

        service.schedule(service::shutdown, 4, TimeUnit.SECONDS);
        service.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(count200.get()).isGreaterThan(3);
    }

    @Test
    public void randomIP() throws InterruptedException {
        List<Throttle> throttles = Collections.singletonList(
                new Throttle("IP", new LimitRate(1, Duration.ofMillis(500L)), HttpRequest::getRemoteAddr)
        );

        ThrottlingMiddleware middleware = builder(new ThrottlingMiddleware())
                .set(ThrottlingMiddleware::setThrottles, throttles)
                .build();

        DefaultHttpRequest req = new DefaultHttpRequest();
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain(new AnyPredicate(), null,
                (Endpoint<HttpRequest, HttpResponse>) request -> HttpResponse.of(""));

        final AtomicInteger count429 = new AtomicInteger(0);
        ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
        service.scheduleAtFixedRate(() -> {
            req.setRemoteAddr(RandomUtils.nextInt(1, 255)
                    + "." + RandomUtils.nextInt(1, 255)
                    + "." + RandomUtils.nextInt(1, 255)
                    + "." + RandomUtils.nextInt(1, 255)
            );
            HttpResponse res = middleware.handle(req, chain);
            if (res.getStatus() == 429) {
                count429.addAndGet(1);
            }
        }, 0, 250, TimeUnit.MILLISECONDS);

        service.schedule(service::shutdown, 3, TimeUnit.SECONDS);
        service.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(count429.get()).isEqualTo(0);
    }
}
