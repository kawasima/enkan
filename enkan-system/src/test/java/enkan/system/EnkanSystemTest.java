package enkan.system;

import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static enkan.component.ComponentRelationship.component;
import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
class EnkanSystemTest {
    @Test
    void systemCreation() {
        SystemComponent c1 = new SystemComponent() {
            @Override
            public ComponentLifecycle lifecycle() {
                return null;
            }
        };
        SystemComponent c2 = new SystemComponent() {
            @Override
            public ComponentLifecycle lifecycle() {
                return null;
            }

            @Override
            public String toString() {
                return "c2 component";
            }
        };
        EnkanSystem system = EnkanSystem.of("c1", c1, "c2", c2);
        assertThat(system.getAllComponents().size()).isEqualTo(2);
        assertThat(system.getComponent("c2").toString()).isEqualTo("c2 component");
    }

    @Test
    void modifySystem() {
        Bar bar = new Bar();
        Baz baz = new Baz();
        EnkanSystem system = EnkanSystem.of("c1", new Foo(), "c2", bar)
                .relationships(component("c1").using("c2"));
        system.setComponent("c3", baz);
        system.relationships(component("c1").using("c3"));
        assertThat(system.getComponent("c1", Foo.class))
                .hasFieldOrPropertyWithValue("bar", bar)
                .hasFieldOrPropertyWithValue("baz", baz);
    }

    private static class Foo extends SystemComponent<Foo> {
        @Inject
        Bar bar;

        @Inject
        Baz baz;

        public Bar getBar() {
            return bar;
        }

        public Baz getBaz() {
            return baz;
        }

        @Override
        protected ComponentLifecycle<Foo> lifecycle() {
            return new ComponentLifecycle<Foo>() {
                @Override
                public void start(Foo component) {

                }

                @Override
                public void stop(Foo component) {

                }
            };
        }
    }

    private static class Bar extends SystemComponent<Bar> {
        @Override
        protected ComponentLifecycle<Bar> lifecycle() {
            return new ComponentLifecycle<Bar>() {
                @Override
                public void start(Bar component) {

                }

                @Override
                public void stop(Bar component) {

                }
            };
        }
    }
    private static class Baz extends SystemComponent<Baz> {
        @Override
        protected ComponentLifecycle<Baz> lifecycle() {
            return new ComponentLifecycle<Baz>() {
                @Override
                public void start(Baz component) {

                }

                @Override
                public void stop(Baz component) {

                }
            };
        }
    }

}
