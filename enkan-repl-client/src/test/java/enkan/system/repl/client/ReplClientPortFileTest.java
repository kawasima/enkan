package enkan.system.repl.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ReplClient.readPortFile() via reflection,
 * exercising the $HOME/.enkan-repl-port discovery logic.
 */
class ReplClientPortFileTest {
    private static final Path PORT_FILE = Path.of(System.getProperty("user.home"), ".enkan-repl-port");

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(PORT_FILE);
    }

    private int readPortFile() throws Exception {
        Method m = ReplClient.class.getDeclaredMethod("readPortFile");
        m.setAccessible(true);
        return (int) m.invoke(null);
    }

    @Test
    void returnsMinus1WhenFileAbsent() throws Exception {
        Files.deleteIfExists(PORT_FILE);

        assertThat(readPortFile()).isEqualTo(-1);
    }

    @Test
    void returnsPortWhenFileContainsValidPort() throws Exception {
        Files.writeString(PORT_FILE, "12345");

        assertThat(readPortFile()).isEqualTo(12345);
    }

    @Test
    void tripsWhitespaceAroundPortNumber() throws Exception {
        Files.writeString(PORT_FILE, "  54321\n");

        assertThat(readPortFile()).isEqualTo(54321);
    }

    @Test
    void returnsMinus1WhenFileContainsNonNumeric() throws Exception {
        Files.writeString(PORT_FILE, "not-a-port");

        assertThat(readPortFile()).isEqualTo(-1);
    }

    @Test
    void returnsMinus1WhenFileIsEmpty() throws Exception {
        Files.writeString(PORT_FILE, "");

        assertThat(readPortFile()).isEqualTo(-1);
    }
}
