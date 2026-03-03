package enkan.component.jetty;

import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.Test;

import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JettyComponentTest {
    @Test
    void parameterValidation() {
        assertThatThrownBy(() -> builder(new JettyComponent())
                .set(JettyComponent::setPort, 77777)
                .build())
                .isInstanceOf(MisconfigurationException.class);
    }
}
