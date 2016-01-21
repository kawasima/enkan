package enkan.util;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author kawasima
 */
public class CodecUtilsTest {
    @Test
    public void parseBytes() {
        byte[] b = CodecUtils.parseBytes("%e3%81%82%e3%81%84%e3%81%86");
        Assert.assertThat(b, is(new byte[]{
                (byte) 0xE3, (byte) 0x81, (byte) 0x82,
                (byte) 0xE3, (byte) 0x81, (byte) 0x84,
                (byte) 0xE3, (byte) 0x81, (byte) 0x86,
        }));
    }

    @Test
    public void urlEncode() {
        Assert.assertThat(CodecUtils.urlEncode("abcあいうdef"),
                is("abc%e3%81%82%e3%81%84%e3%81%86def"));
    }


}
