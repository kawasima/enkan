package enkan.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class CodecUtilsTest {
    @Test
    public void parseBytes() {
        byte[] b = CodecUtils.parseBytes("%e3%81%82%e3%81%84%e3%81%86");
        assertThat(b).isEqualTo(new byte[]{
                (byte) 0xE3, (byte) 0x81, (byte) 0x82,
                (byte) 0xE3, (byte) 0x81, (byte) 0x84,
                (byte) 0xE3, (byte) 0x81, (byte) 0x86,
        });
    }

    @Test
    public void urlEncode() {
        assertThat(CodecUtils.urlEncode("abcあいうdef"))
                .isEqualTo("abc%e3%81%82%e3%81%84%e3%81%86def");
    }

    @Test
    public void formEncode() {
        Map<String, Object> m = new HashMap<>();
        m.put("a", null);
        assertThat(CodecUtils.formEncode(m)).isEqualTo("a=");
    }

    @Test
    public void formEncodeListParameter() {
        Map<String, Object> m = new HashMap<>();
        m.put("a", Arrays.asList("1", "2", "3"));
        assertThat(CodecUtils.formEncode(m)).isEqualTo("a=1&a=2&a=3");
    }

    @Test
    public void formEncodeSetParameter() {
        Map<String, Object> m = new HashMap<>();
        m.put("a", new TreeSet<>(Arrays.asList("1", "2", "3")));
        assertThat(CodecUtils.formEncode(m)).isEqualTo("a=1&a=2&a=3");
    }

}
