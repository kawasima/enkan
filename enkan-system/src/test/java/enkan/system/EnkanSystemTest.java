package enkan.system;

import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class EnkanSystemTest {
    @Test
    public void systemCreation() {
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
}
