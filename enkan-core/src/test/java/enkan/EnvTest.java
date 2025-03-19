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
        System.setProperty("boolean.prop", "true");
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
        assertThat(Env.getString("aaa.bbb", "default")).isEqualTo("1234");
        assertThat(Env.getString("ccc.ddd", "default")).isEqualTo("5678");
        assertThat(Env.getString("nonexistent", "default")).isEqualTo("default");
    }

    @Test
    public void fromPropertyString() {
        assertThat(Env.getString("test.prop", "default")).isEqualTo("1234");
        assertThat(Env.getString("nonexistent", "default")).isEqualTo("default");
    }

    @Test
    public void fromPropertyBoolean() {
        assertThat(Env.getBoolean("boolean.prop", false)).isTrue();
        assertThat(Env.getBoolean("nonexistent", false)).isFalse();
    }

    @Test
    public void fromPropertyInvalid() {
        System.setProperty("invalid.prop", "not_a_number");
        assertThatThrownBy(() -> Env.getInt("invalid.prop", 3))
            .isInstanceOf(NumberFormatException.class);

        assertThatThrownBy(() -> Env.getLong("invalid.prop", 3L))
            .isInstanceOf(NumberFormatException.class);
    }
}
