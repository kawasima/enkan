package enkan.component.undertow;

import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.Test;

import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author kawasima
 */
class UndertowComponentTest {
    @Test
    void parameterValidation() {
        assertThatThrownBy(() -> builder(new UndertowComponent())
                .set(UndertowComponent::setPort, 77777)
                .build())
                .isInstanceOf(MisconfigurationException.class);
    }
}
