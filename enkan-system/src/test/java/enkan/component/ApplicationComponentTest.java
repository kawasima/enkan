package enkan.component;

import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class ApplicationComponentTest {
    @Test
    public void validationError() {
        assertThatThrownBy(() -> {
            ApplicationComponent component = new ApplicationComponent(TestApplicationFactory.class.getName());
            component.lifecycle().start(component);
        }).isExactlyInstanceOf(MisconfigurationException.class);
    }
}
