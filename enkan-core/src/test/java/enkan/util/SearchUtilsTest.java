package enkan.util;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class SearchUtilsTest {
    ByteBuffer buf = ByteBuffer.wrap(new byte[]{
            3,1,4,1,5,9,2,6,5,3,
            5,8,9,7,9,3,2,3,8,4,
            6,2,6,4,3,3,8,3,2,7,
            9,5,0,2,8,8});

    @Test
    public void match() {
        byte[] sought = new byte[]{ 3,3,8,3 };
        assertThat(SearchUtils.kmp(buf, sought)).isEqualTo(24);
    }

    @Test
    public void notMatch() {
        byte[] sought = new byte[]{ 3,3,9,3 };
        assertThat(SearchUtils.kmp(buf, sought)).isEqualTo(-1);
    }

    @Test
    public void empty() {
        byte[] sought = new byte[]{};
        assertThat(SearchUtils.kmp(buf, sought)).isEqualTo(-1);
    }

    @Test
    public void oneByte() {
        byte[] sought = new byte[]{9};
        assertThat(SearchUtils.kmp(buf, sought)).isEqualTo(5);
    }

    @Test
    public void directBuf() {
        ByteBuffer directBuf = ByteBuffer.allocateDirect(100);
        directBuf.put(buf.array());
        directBuf.flip();

        byte[] sought = new byte[]{ 3,3,8,3 };
        assertThat(SearchUtils.kmp(directBuf, sought)).isEqualTo(24);
    }
}
