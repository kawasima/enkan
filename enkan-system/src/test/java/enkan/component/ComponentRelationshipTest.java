package enkan.component;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class ComponentRelationshipTest {
    @Test
    public void sort() {
        ComponentRelationship r1 = ComponentRelationship.component("A").using("B", "C");
        LinkedList<String> componentOrder = new LinkedList<>(Arrays.asList("A", "B", "C"));
        r1.sort(componentOrder);
        assertEquals(Arrays.asList("B", "C", "A"), componentOrder);
    }
}
