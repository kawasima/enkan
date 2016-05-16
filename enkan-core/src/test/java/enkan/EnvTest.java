package enkan;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class EnvTest {
    @BeforeClass
    public static void setupProperties() {
        System.setProperty("test.prop", "1234");
        // Overwrite the value from file
        System.setProperty("ccc.ddd", "5678");
    }

    @Test
    public void fromProperty() {

        assertEquals(1234, Env.getInt("TEST_PROP", 3));
        assertEquals(1234, Env.getInt("test.prop", 3));
        assertEquals(3, Env.getInt("test-prop", 3));
    }

    @Test
    public void fromFile() {
        assertEquals(1234, Env.getLong("aaa.bbb", 3));
        assertEquals(5678, Env.getLong("ccc.ddd", 0));
    }
}
