package enkan.collection;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author kawasima
 */
public class ParametersTest {
    @Test
    public void test() {
        Parameters params = Parameters.empty();

        Parameters n1 = Parameters.empty();
        n1.put("Foo", "Bar");
        n1.put("Fizz", "Buzz");
        List<String> v1 = new ArrayList<>();

        v1.add("AAA");
        v1.add("BBB");
        v1.add("CCC");
        params.put("N1", n1);
        params.put("V1", v1);

        assertEquals("CCC", params.getIn("V1", 2));
        assertEquals("BBB", params.getIn("V1", "1"));
        assertNull("0.0 isn't a integer format", params.getIn("V1", "0.0"));
        assertEquals("Buzz", params.getIn("N1", "Fizz"));
    }
}
