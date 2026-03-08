package enkan.system.repl.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ReplClient.ReplClient.readPortFile().
 * Redirects user.home to a JUnit-managed temp directory so the real
 * $HOME/.enkan-repl-port is never touched.
 */
class ReplClientPortFileTest {
    @TempDir
    Path tempDir;

    private String originalHome;

    @BeforeEach
    void redirectHome() {
        originalHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
    }

    @AfterEach
    void restoreHome() {
        System.setProperty("user.home", originalHome);
    }

    private Path portFile() {
        return tempDir.resolve(".enkan-repl-port");
    }

    @Test
    void returnsMinus1WhenFileAbsent() throws Exception {
        assertThat(ReplClient.readPortFile()).isEqualTo(-1);
    }

    @Test
    void returnsPortWhenFileContainsValidPort() throws Exception {
        Files.writeString(portFile(), "12345");

        assertThat(ReplClient.readPortFile()).isEqualTo(12345);
    }

    @Test
    void trimsWhitespaceAroundPortNumber() throws Exception {
        Files.writeString(portFile(), "  54321\n");

        assertThat(ReplClient.readPortFile()).isEqualTo(54321);
    }

    @Test
    void returnsMinus1WhenFileContainsNonNumeric() throws Exception {
        Files.writeString(portFile(), "not-a-port");

        assertThat(ReplClient.readPortFile()).isEqualTo(-1);
    }

    @Test
    void returnsMinus1WhenFileIsEmpty() throws Exception {
        Files.writeString(portFile(), "");

        assertThat(ReplClient.readPortFile()).isEqualTo(-1);
    }

    @Test
    void returnsMinus1WhenPortIsOutOfRange() throws Exception {
        Files.writeString(portFile(), "99999");

        assertThat(ReplClient.readPortFile()).isEqualTo(-1);
    }

    @Test
    void returnsMinus1WhenPortIsZero() throws Exception {
        Files.writeString(portFile(), "0");

        assertThat(ReplClient.readPortFile()).isEqualTo(-1);
    }
}
