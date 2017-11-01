package enkan.system.inject;

import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class ComponentInjectorTest {
    private Map<String, SystemComponent> componentMap;

    @BeforeEach
    public void setup() {
        componentMap = new HashMap<>();
    }

    @Test
    public void test() {
        componentMap.put("myNameIsA", new TestComponent("A"));

        ComponentInjector injector = new ComponentInjector(componentMap);
        InjectTarget1 target1 = new InjectTarget1();
        injector.inject(target1);
        assertThat(target1.tc.getId()).isEqualTo("A");
    }

    @Test
    public void testNamedInject() {
        componentMap.put("myNameIsA", new TestComponent("A"));
        componentMap.put("myNameIsB", new TestComponent("B"));

        ComponentInjector injector = new ComponentInjector(componentMap);
        InjectTarget2 target2 = new InjectTarget2();
        injector.inject(target2);
        assertThat(target2.tc.getId()).isEqualTo("B");
    }

    @Test
    public void wrongNamedInject() {
        componentMap.put("myNameIsAAA", new TestComponent("A"));
        componentMap.put("MyNameIsB", new TestComponent("B"));

        ComponentInjector injector = new ComponentInjector(componentMap);
        InjectTarget2 target2 = new InjectTarget2();
        try {
            injector.inject(target2);
            fail("MisconfigurationException will occur");
        } catch (MisconfigurationException ex) {
            assertThat(ex.getSolution().contains("MyNameIsB")).isTrue();
        }
    }

    private static class InjectTarget1 {
        @Inject
        TestComponent tc;
    }

    private static class InjectTarget2 {
        @Inject
        @Named("myNameIsB")
        TestComponent tc;
    }

    private static class TestComponent extends SystemComponent {
        private String id;
        public TestComponent(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        protected ComponentLifecycle<TestComponent> lifecycle() {
            return new ComponentLifecycle<TestComponent>() {
                @Override
                public void start(TestComponent component) {

                }

                @Override
                public void stop(TestComponent component) {

                }
            };
        }
    }
}
