package enkan.util;


import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Optional;

import static enkan.util.ThreadingUtils.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class ThreadingUtilsTest {
    @Test
    public void nullInChain() {
        Optional<String> booleanName = ThreadingUtils.some(System.getenv(),
                env -> env.get("HOME1"), // Null
                String::isEmpty,
                Object::toString);
        assertThat(booleanName.isPresent()).isFalse();
    }

    @Test
    public void file() {
        String path = "^/hoge";
        Optional<URL> url = ThreadingUtils.some(path, File::new, File::toURI, URI::toURL);
        assertThat(url.isPresent()).isTrue();
    }

    @Test
    public void urlEncode() {
        String str = "あいうえお";

        Optional<String> encoded = some(str,
                partial(URLEncoder::encode, "UTF-8"));
        assertThat(encoded.isPresent()).isTrue();

        str = null;
        //noinspection ConstantConditions
        encoded = ThreadingUtils.some(str, s -> URLEncoder.encode(s, "UTF-8"));
        assertThat(encoded.isPresent()).isFalse();
    }
}
