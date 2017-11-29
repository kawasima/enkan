package kotowari.middleware.serdes;

import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class ToStringBodyWriterTest {
    @Test
    public void test() throws IOException {
        ToStringBodyWriter bw = new ToStringBodyWriter();
        List<String> list = Arrays.asList("apple", "orange", "banana");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bw.writeTo(list, null, null, null, MediaType.TEXT_PLAIN_TYPE, null, baos);
        assertThat(new String(baos.toByteArray())).isEqualTo("[apple, orange, banana]");

        baos.reset();
        bw.writeTo(list, null, null, null, MediaType.TEXT_HTML_TYPE, null, baos);
        assertThat(new String(baos.toByteArray())).isEqualTo("<html><body>[apple, orange, banana]</body></html>");

        baos.reset();
        bw.writeTo(list, null, null, null, MediaType.TEXT_XML_TYPE, null, baos);
        assertThat(new String(baos.toByteArray())).isEqualTo("<message>[apple, orange, banana]</message>");

        baos.reset();
        bw.writeTo(list, null, null, null, MediaType.WILDCARD_TYPE, null, baos);
        assertThat(new String(baos.toByteArray())).isEqualTo("[apple, orange, banana]");
    }
}
