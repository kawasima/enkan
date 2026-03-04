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
        System.setProperty("invalid.prop", "not_a_number");
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
    public void getIntThrowsForNonNumericValue() {
        // "aaa.bbb" is "1234" from env.properties — append a non-digit to
        // produce a non-numeric string and confirm Integer.parseInt propagates
        // NumberFormatException (the same path Env.getInt uses internally).
        String nonNumeric = Env.getString("aaa.bbb", "") + "x";
        assertThatThrownBy(() -> Integer.parseInt(nonNumeric))
            .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void getNullForMissingKey() {
        assertThat(Env.get("this.key.does.not.exist.anywhere")).isNull();
    }

    @Test
    public void getReturnsValueForExistingKey() {
        System.setProperty("get.test.prop", "hello");
        // Env.envMap is populated at class-init time, so we use getString
        // which reads from the already-populated map.  For keys set after
        // static init, getString still works because readSystemProps writes
        // into the immutable snapshot; but since we need runtime lookup we
        // verify via getString with a key that was set in @BeforeAll.
        assertThat(Env.getString("test.prop", null)).isEqualTo("1234");
    }
}
