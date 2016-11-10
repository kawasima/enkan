package enkan.system.inject;

import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import enkan.exception.MisconfigurationException;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class ComponentInjectorTest {
    private Map<String, SystemComponent> componentMap;

    @Before
    public void setup() {
        componentMap = new HashMap<>();
    }

    @Test
    public void test() {
        componentMap.put("myNameIsA", new TestComponent("A"));

        ComponentInjector injector = new ComponentInjector(componentMap);
        InjectTarget1 target1 = new InjectTarget1();
        injector.inject(target1);
        assertEquals("A", target1.tc.getId());
    }

    @Test
    public void testNamedInject() {
        componentMap.put("myNameIsA", new TestComponent("A"));
        componentMap.put("myNameIsB", new TestComponent("B"));

        ComponentInjector injector = new ComponentInjector(componentMap);
        InjectTarget2 target2 = new InjectTarget2();
        injector.inject(target2);
        assertEquals("B", target2.tc.getId());
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
            assertTrue(ex.getSolution().contains("MyNameIsB"));
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
