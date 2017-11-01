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
        assertThat(values.get(0)).isEqualTo("bbb");

        mm.add("aaa", "ccc");
        assertThat(mm.get("aaa")).isEqualTo("bbb");

        values = mm.getAll("aaa");
        assertThat(values.size()).isEqualTo(2);
        assertThat(values.get(1)).isEqualTo("ccc");
    }
}
