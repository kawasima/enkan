package enkan.exception;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kawasima
 */
public class MisconfigurationExceptionTest {
    private static Logger LOG = LoggerFactory.getLogger(MisconfigurationExceptionTest.class);

    @Test
    public void test() {
        try {
            MisconfigurationException.raise("MIDDLEWARE_DEPENDENCY", "A", "B");
        } catch (MisconfigurationException ex) {
            System.out.println(ex.getProblem());
            System.out.println(ex.getSolution());
        }
    }
}
