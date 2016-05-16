package enkan.util;


import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Optional;

import static enkan.util.ThreadingUtils.partial;
import static enkan.util.ThreadingUtils.some;
import static org.junit.Assert.*;

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
        assertFalse(booleanName.isPresent());
    }

    @Test
    public void file() {
        String path = "^/hoge";
        Optional<URL> url = ThreadingUtils.some(path, File::new, File::toURI, URI::toURL);
        assertTrue(url.isPresent());
    }

    @Test
    public void urlEncode() {
        String str = "あいうえお";

        Optional<String> encoded = some(str,
                partial(URLEncoder::encode, "UTF-8"));
        assertTrue(encoded.isPresent());

        str = null;
        encoded = ThreadingUtils.some(str, s -> URLEncoder.encode(s, "UTF-8"));
        Assert.assertFalse(encoded.isPresent());
    }

}
