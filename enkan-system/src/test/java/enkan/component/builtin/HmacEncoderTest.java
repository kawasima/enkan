package enkan.component.builtin;

import enkan.exception.MisconfigurationException;
import enkan.system.EnkanSystem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test cases for HmacEncoder component.
 * Tests include:
 * - Default encoding with HmacSHA256
 * - Different HMAC algorithms (MD5, SHA1)
 * - Custom secret key
 * - Invalid algorithm handling
 * - Special input handling
 * - String representation
 */
public class HmacEncoderTest {
    @Test
    public void testDefaultEncoding() {
        EnkanSystem system = EnkanSystem.of("hmac", new HmacEncoder());
        system.start();
        try {
            HmacEncoder encoder = system.getComponent("hmac");
            assertThat(encoder.encodeToHex("12345"))
                    .isEqualTo("E20F9FB4EDE5059B4F0FC18CA4B0BB804060422DE2AA764119DE11BE8EE5A586");
        } finally {
            system.stop();
        }
    }

    @Test
    public void testDifferentAlgorithms() {
        HmacEncoder encoder = new HmacEncoder();
        
        // HmacMD5 produces 128-bit (32 hex characters) output
        encoder.setAlgorithm("HmacMD5");
        String md5Result = encoder.encodeToHex("12345");
        assertThat(md5Result).hasSize(32);

        // HmacSHA1 produces 160-bit (40 hex characters) output
        encoder.setAlgorithm("HmacSHA1");
        String sha1Result = encoder.encodeToHex("12345");
        assertThat(sha1Result).hasSize(40);
    }

    @Test
    public void testCustomSecret() {
        HmacEncoder encoder = new HmacEncoder();
        encoder.setSecret("MyCustomSecret");
        String result = encoder.encodeToHex("12345");
        
        // Custom secret should produce different result than default
        assertThat(result)
                .isNotEqualTo("E20F9FB4EDE5059B4F0FC18CA4B0BB804060422DE2AA764119DE11BE8EE5A586")
                .hasSize(64); // SHA256 produces 256-bit (64 hex characters) output
    }

    @Test
    public void testInvalidAlgorithm() {
        HmacEncoder encoder = new HmacEncoder();
        assertThatThrownBy(() -> {
            encoder.setAlgorithm("InvalidAlgorithm");
            encoder.encodeToHex("12345");
        })
                .isInstanceOf(MisconfigurationException.class)
                .hasMessageContaining("NO_SUCH_ALGORITHM");
    }

    @Test
    public void testSpecialInputs() {
        HmacEncoder encoder = new HmacEncoder();
        
        // Empty string
        assertThat(encoder.encodeToHex(""))
                .hasSize(64)
                .matches("[0-9A-F]{64}");

        // String with special characters
        assertThat(encoder.encodeToHex("!@#$%^&*()_+"))
                .hasSize(64)
                .matches("[0-9A-F]{64}");

        // Unicode string
        assertThat(encoder.encodeToHex("こんにちは世界"))
                .hasSize(64)
                .matches("[0-9A-F]{64}");
    }

    @Test
    public void testToString() {
        HmacEncoder encoder = new HmacEncoder();
        String str = encoder.toString();
        assertThat(str)
                .contains("HmacEncoder")
                .contains("algorithm")
                .contains("HmacSHA256");
    }
}
