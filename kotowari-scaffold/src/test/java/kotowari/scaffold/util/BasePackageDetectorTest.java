package kotowari.scaffold.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author kawasima
 */
public class BasePackageDetectorTest {
    @Before
    public void setup() throws IOException {
        Files.createDirectories(Paths.get("target/basedetect/db/migration"));
        Files.createFile(Paths.get("target/basedetect/db/migration/Mig.java"));

        Files.createDirectories(Paths.get("target/basedetect/hoge/fuga/piyo"));
        Files.createFile(Paths.get("target/basedetect/hoge/fuga/App.java"));
    }

    @Test
    public void test() throws IOException {
        String path = BasePackageDetector.detect("target/basedetect");
        Assert.assertEquals("hoge.fuga.", path);
    }

    @After
    public void deleteDirectories() throws IOException {
        Files.deleteIfExists(Paths.get("target/basedetect/db/migration/Mig.java"));
        Files.deleteIfExists(Paths.get("target/basedetect/db/migration"));
        Files.deleteIfExists(Paths.get("target/basedetect/db"));

        Files.deleteIfExists(Paths.get("target/basedetect/hoge/fuga/App.java"));
        Files.deleteIfExists(Paths.get("target/basedetect/hoge/fuga/piyo"));
        Files.deleteIfExists(Paths.get("target/basedetect/hoge/fuga"));
        Files.deleteIfExists(Paths.get("target/basedetect/hoge"));
        Files.deleteIfExists(Paths.get("target/basedetect"));
    }
}
