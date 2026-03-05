package enkan.collection;

import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(params.getIn("V1", 2)).isEqualTo("CCC");
        assertThat(params.getIn("V1", "1")).isEqualTo("BBB");
        assertThat(params.getIn("V1", "0.0"))
                .as("0.0 isn't a integer format")
                .isNull();
        assertThat(params.getIn("N1", "Fizz")).isEqualTo("Buzz");
    }

    @Test
    void putAllAppliesCaseNormalization() {
        Parameters params = Parameters.empty();
        params.setCaseSensitive(false);
        params.put("name", "original");

        Map<String, Object> extra = new HashMap<>();
        extra.put("NAME", "updated");

        params.putAll(extra);

        // putAll should normalize "NAME" to "name" and merge via put()
        assertThat(params.size()).isEqualTo(1);
        assertThat(params.containsKey("name")).isTrue();
    }

    @Test
    void ofWithOddArgumentsThrowsMisconfigurationException() {
        assertThatThrownBy(() -> Parameters.of("key1", "val1", "key2"))
                .isInstanceOf(MisconfigurationException.class);
    }
}
