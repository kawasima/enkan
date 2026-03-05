package enkan.system.inject;

import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class ComponentInjectorTest {
    private Map<String, SystemComponent<?>> componentMap;

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
    public void constructorInjection() {
        componentMap.put("myNameIsA", new TestComponent("A"));

        ComponentInjector injector = new ComponentInjector(componentMap);
        ConstructorInjectionTarget target = injector.newInstance(ConstructorInjectionTarget.class);
        assertThat(target.tc.getId()).isEqualTo("A");
    }

    @Test
    public void namedConstructorInjection() {
        componentMap.put("myNameIsA", new TestComponent("A"));
        componentMap.put("myNameIsB", new TestComponent("B"));

        ComponentInjector injector = new ComponentInjector(componentMap);
        NamedConstructorInjectionTarget target = injector.newInstance(NamedConstructorInjectionTarget.class);
        assertThat(target.tc.getId()).isEqualTo("B");
    }

    @Test
    public void fallbackToFieldInjectionWhenNoInjectConstructor() {
        componentMap.put("myNameIsA", new TestComponent("A"));

        ComponentInjector injector = new ComponentInjector(componentMap);
        NoInjectConstructorTarget target = injector.newInstance(NoInjectConstructorTarget.class);
        assertThat(target.tc.getId()).isEqualTo("A");
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

    // --- Constructor injection targets ---

    private static class ConstructorInjectionTarget {
        final TestComponent tc;

        @Inject
        ConstructorInjectionTarget(TestComponent tc) {
            this.tc = tc;
        }
    }

    private static class NamedConstructorInjectionTarget {
        final TestComponent tc;

        @Inject
        NamedConstructorInjectionTarget(@Named("myNameIsB") TestComponent tc) {
            this.tc = tc;
        }
    }

    private static class NoInjectConstructorTarget {
        @Inject
        TestComponent tc;

        public NoInjectConstructorTarget() {}
    }

    private static class TestComponent extends SystemComponent<TestComponent> {
        private final String id;
        public TestComponent(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        protected ComponentLifecycle<TestComponent> lifecycle() {
            return new ComponentLifecycle<>() {
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
