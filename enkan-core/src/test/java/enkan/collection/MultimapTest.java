package enkan.collection;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author kawasima
 */
public class MultimapTest {
    @Test
    public void getAndPut() {
        Multimap<String, String> mm = Multimap.empty();
        assertNull(mm.get("aaa"));

        mm.put("aaa", "bbb");
        assertEquals("bbb", mm.get("aaa"));
        List<String> values = mm.getAll("aaa");
        assertEquals(1, values.size());
        assertEquals("bbb", values.get(0));

        mm.add("aaa", "ccc");
        assertEquals("bbb", mm.get("aaa"));
        values = mm.getAll("aaa");
        assertEquals(2, values.size());
        assertEquals("ccc", values.get(1));

    }
}
