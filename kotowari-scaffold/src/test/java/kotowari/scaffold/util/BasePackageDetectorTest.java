package kotowari.scaffold.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author kawasima
 */
class BasePackageDetectorTest {
    @BeforeEach
    void setup() throws IOException {
        Files.createDirectories(Paths.get("target/basedetect/db/migration"));
        Files.createFile(Paths.get("target/basedetect/db/migration/Mig.java"));

        Files.createDirectories(Paths.get("target/basedetect/hoge/fuga/piyo"));
        Files.createFile(Paths.get("target/basedetect/hoge/fuga/App.java"));
    }

    @Test
    void test() throws IOException {
        Assertions.assertThat(BasePackageDetector.detect("target/basedetect"))
                .isEqualTo("hoge.fuga.");
    }

    @AfterEach
    void deleteDirectories() throws IOException {
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
