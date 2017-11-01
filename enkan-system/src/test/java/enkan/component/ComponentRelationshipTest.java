package enkan.component;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class ComponentRelationshipTest {
    @Test
    public void sort() {
        ComponentRelationship r1 = ComponentRelationship.component("A").using("B", "C");
        LinkedList<String> componentOrder = new LinkedList<>(Arrays.asList("A", "B", "C"));
        r1.sort(componentOrder);
        assertThat(componentOrder).isEqualTo(Arrays.asList("B", "C", "A"));
    }
}
