package enkan.exception;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author kawasima
 */
public class MisconfigurationExceptionTest {
    @Test
    public void test() {
        try {
            throw MisconfigurationException.create("MIDDLEWARE_DEPENDENCY", "A", "B");
        } catch (MisconfigurationException ex) {
            assertNotNull(ex.getProblem());
            assertNotNull(ex.getSolution());
        }
    }
}
