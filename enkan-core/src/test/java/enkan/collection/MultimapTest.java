package enkan.collection;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class MultimapTest {
    @Test
    public void getAndPut() {
        Multimap<String, String> mm = Multimap.empty();
        assertThat(mm.get("aaa")).isNull();

        mm.put("aaa", "bbb");
        assertThat(mm.get("aaa")).isEqualTo("bbb");

        List<String> values = mm.getAll("aaa");
        assertThat(values.size()).isEqualTo(1);
        assertThat(values.getFirst()).isEqualTo("bbb");

        mm.add("aaa", "ccc");
        assertThat(mm.get("aaa")).isEqualTo("bbb");

        values = mm.getAll("aaa");
        assertThat(values.size()).isEqualTo(2);
        assertThat(values.get(1)).isEqualTo("ccc");
    }

    @Test
    public void getAllReturnsEmptyListForMissingKey() {
        Multimap<String, String> mm = Multimap.empty();
        List<String> values = mm.getAll("missing");
        assertThat(values).isNotNull().isEmpty();
    }

    @Test
    public void ofFactoryCreatesExpectedEntries() {
        Multimap<String, Integer> mm = Multimap.of("a", 1, "b", 2, "c", 3, "d", 4);
        assertThat(mm.get("a")).isEqualTo(1);
        assertThat(mm.get("b")).isEqualTo(2);
        assertThat(mm.get("c")).isEqualTo(3);
        assertThat(mm.get("d")).isEqualTo(4);
    }

    @Test
    public void putOverwritesPreviousValues() {
        Multimap<String, String> mm = Multimap.empty();
        mm.add("k", "first");
        mm.add("k", "second");
        assertThat(mm.getAll("k")).containsExactly("first", "second");

        // put() replaces all values for the key
        mm.put("k", "only");
        assertThat(mm.getAll("k")).containsExactly("only");
    }
}
