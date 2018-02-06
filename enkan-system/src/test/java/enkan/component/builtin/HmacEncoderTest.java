package enkan.component.builtin;

import enkan.system.EnkanSystem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HmacEncoderTest {
    @Test
    public void encode() {
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
}
