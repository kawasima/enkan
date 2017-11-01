package enkan.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class MisconfigurationExceptionTest {
    @Test
    public void test() {
        try {
            throw new MisconfigurationException("core.MIDDLEWARE_DEPENDENCY", "A", "B");
        } catch (MisconfigurationException ex) {
            assertThat(ex.getProblem()).isNotNull();
            assertThat(ex.getSolution()).isNotNull();
        }
    }
}
