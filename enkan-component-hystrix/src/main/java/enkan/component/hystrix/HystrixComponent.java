package enkan.component.hystrix;

import com.netflix.hystrix.Hystrix;
import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;

/**
 * @author kawasima
 */
public class HystrixComponent extends SystemComponent {
    @Override
    protected ComponentLifecycle<HystrixComponent> lifecycle() {
        return new ComponentLifecycle<HystrixComponent>() {
            @Override
            public void start(HystrixComponent component) {
            }

            @Override
            public void stop(HystrixComponent component) {
                Hystrix.reset();
            }
        };
    }
}
