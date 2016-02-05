package enkan.collection;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class NestedParamsTest {
    @Test
    public void test() {
        NestedParams<String> params = new MapNestedParams();

        NestedParams<String> n1 = new MapNestedParams();
        n1.put("Foo", "Bar");
        n1.put("Fizz", "Buzz");
        VectorNestedParams v1 = new VectorNestedParams();
        v1.add("AAA");
        v1.add("BBB");
        v1.add("CCC");
        params.put("N1", n1);
        params.put("V1", v1);

        assertEquals("CCC", params.getIn("V1", 2));
        assertEquals("BBB", params.getIn("V1", "1"));
        assertNull("0.0 isn't a integer format", params.getIn("V1", "0.0"));
        assertNull(v1.getIn("V1", 3));
        assertEquals("Buzz", params.getIn("N1", "Fizz"));
    }
}
