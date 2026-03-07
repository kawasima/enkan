package enkan.component.undertow;

import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author kawasima
 */
class UndertowComponentTest {
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
        assertThatThrownBy(() -> builder(new UndertowComponent())
                .set(UndertowComponent::setPort, 77777)
                .build())
                .isInstanceOf(MisconfigurationException.class);
    }
}
