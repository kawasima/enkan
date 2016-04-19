package enkan.component.hystrix;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kawasima on 16/02/22.
 */
public class HystrixTest {
    private void callApi() {
        HystrixRequestContext ctx = HystrixRequestContext.initializeContext();
        new FooCommand().execute();
        System.out.println(HystrixRequestLog.getCurrentRequest());
        System.out.println(HystrixRequestLog.getCurrentRequest().getExecutedCommandsAsString());
        ctx.shutdown();
    }

    @Test
    public void test() {
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.metrics.rollingPercentile.numBuckets", 1);
        callApi();
        callApi();
        callApi();
        callApi();
        callApi();

        HystrixCommandMetrics metrics = HystrixCommandMetrics.getInstance(HystrixCommandKey.Factory.asKey(FooCommand.class.getSimpleName()));
        System.out.println(metrics.getHealthCounts().getErrorCount());
        callApi();
        callApi();

    }

    public static class FooCommand extends HystrixCommand<Map<String, String>> {
        protected FooCommand() {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("Foo")));
        }

        @Override
        protected Map<String, String> run() throws Exception {
            Thread.sleep(1000);
            if (Math.random() < 0.5) {
                throw new RuntimeException("error");
            }
            return null;
        }

        @Override
        protected Map<String, String> getFallback() {
            return new HashMap<>();
        }
    }
}
