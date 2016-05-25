package enkan.component;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class ComponentRelationshipTest {
    @Test
    public void sort() {
        ComponentRelationship r1 = ComponentRelationship.component("A").using("B", "C");
        List<String> componentOrder = Arrays.asList("A", "B");
        r1.sort(componentOrder);
        assertEquals(Arrays.asList("B", "A"), componentOrder);
    }
}