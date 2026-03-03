package enkan.middleware.metrics;

import com.codahale.metrics.Timer;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.component.LifecycleManager;
import enkan.component.metrics.MetricsComponent;
import enkan.util.Predicates;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetricsMiddlewareTest {

    private MetricsComponent metricsComponent;
    private MetricsMiddleware<Object, Object> middleware;

    @BeforeEach
    void setUp() throws Exception {
        metricsComponent = new MetricsComponent();
        LifecycleManager.start(metricsComponent);

        middleware = new MetricsMiddleware<>();
        Field f = MetricsMiddleware.class.getDeclaredField("metrics");
        f.setAccessible(true);
        f.set(middleware, metricsComponent);
    }

    @AfterEach
    void tearDown() {
        LifecycleManager.stop(metricsComponent);
    }

    /**
     * Builds a two-node chain: MetricsMiddleware -> endpoint.
     * The endpoint is a function from request to response (or throws).
     */
    private MiddlewareChain<Object, Object, Object, Object> chainOf(Function<Object, Object> fn) {
        Middleware<Object, Object, Object, Object> endpoint = new Middleware<>() {
            @Override
            public <NNREQ, NNRES> Object handle(Object req,
                    MiddlewareChain<Object, Object, NNREQ, NNRES> chain) {
                return fn.apply(req);
            }
        };
        DefaultMiddlewareChain<Object, Object, Object, Object> metricsChain =
                new DefaultMiddlewareChain<>(Predicates.any(), "metrics", middleware);
        DefaultMiddlewareChain<Object, Object, Object, Object> endpointChain =
                new DefaultMiddlewareChain<>(Predicates.any(), "endpoint", endpoint);
        metricsChain.setNext(endpointChain);
        return metricsChain;
    }

    @Test
    void activeRequestsIsIncrementedThenDecremented() {
        assertThat(metricsComponent.getActiveRequests().getCount()).isZero();

        AtomicLong inFlightCount = new AtomicLong();
        chainOf(req -> {
            inFlightCount.set(metricsComponent.getActiveRequests().getCount());
            return "res";
        }).next("req");

        assertThat(inFlightCount.get()).isEqualTo(1);
        assertThat(metricsComponent.getActiveRequests().getCount()).isZero();
    }

    @Test
    void requestTimerIsUpdatedOnSuccess() {
        Timer timer = metricsComponent.getRequestTimer();
        assertThat(timer.getCount()).isZero();

        chainOf(req -> "res").next("req");

        assertThat(timer.getCount()).isEqualTo(1);
    }

    @Test
    void errorsAreMeterOnException() {
        assertThat(metricsComponent.getErrors().getCount()).isZero();

        assertThatThrownBy(() ->
                chainOf(req -> { throw new RuntimeException("boom"); }).next("req")
        ).isInstanceOf(RuntimeException.class).hasMessage("boom");

        assertThat(metricsComponent.getErrors().getCount()).isEqualTo(1);
    }

    @Test
    void activeRequestsIsDecrementedEvenOnException() {
        assertThatThrownBy(() ->
                chainOf(req -> { throw new RuntimeException("boom"); }).next("req")
        ).isInstanceOf(RuntimeException.class);

        assertThat(metricsComponent.getActiveRequests().getCount()).isZero();
    }

    @Test
    void timerIsUpdatedEvenOnException() {
        Timer timer = metricsComponent.getRequestTimer();

        assertThatThrownBy(() ->
                chainOf(req -> { throw new RuntimeException("boom"); }).next("req")
        ).isInstanceOf(RuntimeException.class);

        assertThat(timer.getCount()).isEqualTo(1);
    }

    @Test
    void errorsAreNotMarkedOnSuccess() {
        chainOf(req -> "res").next("req");

        assertThat(metricsComponent.getErrors().getCount()).isZero();
    }
}
