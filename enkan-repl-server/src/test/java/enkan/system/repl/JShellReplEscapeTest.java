package enkan.system.repl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code escapeForJShellString} helper in {@link JShellRepl}.
 *
 * <p>The method is {@code private static} so it is accessed via reflection.
 * These tests guard against injection vulnerabilities when user-supplied strings
 * are embedded inside JShell {@code eval()} calls.</p>
 */
public class JShellReplEscapeTest {
    private static Method escape;

    @BeforeAll
    static void findMethod() throws Exception {
        escape = JShellRepl.class.getDeclaredMethod("escapeForJShellString", String.class);
        escape.setAccessible(true);
    }

    private String esc(String s) throws Exception {
        return (String) escape.invoke(null, s);
    }

    @Test
    public void plainStringIsUnchanged() throws Exception {
        assertEquals("hello", esc("hello"));
    }

    @Test
    public void backslashIsDoubled() throws Exception {
        assertEquals("a\\\\b", esc("a\\b"));
    }

    @Test
    public void doubleQuoteIsEscaped() throws Exception {
        assertEquals("say \\\"hi\\\"", esc("say \"hi\""));
    }

    @Test
    public void newlineIsEscaped() throws Exception {
        assertEquals("line1\\nline2", esc("line1\nline2"));
    }

    @Test
    public void carriageReturnIsEscaped() throws Exception {
        assertEquals("a\\rb", esc("a\rb"));
    }

    @Test
    public void tabIsEscaped() throws Exception {
        assertEquals("col1\\tcol2", esc("col1\tcol2"));
    }

    @Test
    public void multipleSpecialCharsEscapedInOrder() throws Exception {
        // backslash must be doubled before quotes are escaped to avoid double-processing
        // input:  path\to"file"
        // after \\ → \\\\: path\\to"file"
        // after "  → \\":  path\\to\"file\"
        assertEquals("path\\\\to\\\"file\\\"", esc("path\\to\"file\""));
    }

    @Test
    public void emptyStringIsUnchanged() throws Exception {
        assertEquals("", esc(""));
    }
}
