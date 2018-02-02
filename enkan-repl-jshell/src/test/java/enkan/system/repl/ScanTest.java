package enkan.system.repl;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Enumeration;

public class ScanTest {
    @Test
    public void test() {
        try {
            Enumeration<URL> urls = ClassLoader.getSystemResources("enkan/system/repl");
            while(urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    reader.lines().forEach(System.out::println);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
