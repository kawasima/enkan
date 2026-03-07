package enkan.component.jetty;

import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JettyComponentTest {
    static boolean isValidationAvailable() {
        try {
            jakarta.validation.Validation.buildDefaultValidatorFactory().close();
            return true;
        } catch (Exception | NoClassDefFoundError e) {
            return false;
        }
    }

    @Test
    @EnabledIf("isValidationAvailable")
    void parameterValidation() {
        assertThatThrownBy(() -> builder(new JettyComponent())
                .set(JettyComponent::setPort, 77777)
                .build())
                .isInstanceOf(MisconfigurationException.class);
    }
}
