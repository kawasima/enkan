package enkan;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class EnvTest {
    @BeforeAll
    public static void setupProperties() {
        System.setProperty("test.prop", "1234");
        // Overwrite the value from file
        System.setProperty("ccc.ddd", "5678");
    }

    @Test
    public void fromProperty() {
        assertThat(Env.getInt("TEST_PROP", 3)).isEqualTo(1234);
        assertThat(Env.getInt("test.prop", 3)).isEqualTo(1234);
        assertThat(Env.getInt("test-prop", 3)).isEqualTo(3);
    }

    @Test
    public void fromFile() {
        assertThat(Env.getLong("aaa.bbb", 3)).isEqualTo(1234);
        assertThat(Env.getLong("ccc.ddd", 0)).isEqualTo(5678);
    }
}
