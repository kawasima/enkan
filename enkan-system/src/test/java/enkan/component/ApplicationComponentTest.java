package enkan.component;

import enkan.exception.MisconfigurationException;
import org.junit.Test;

/**
 * @author kawasima
 */
public class ApplicationComponentTest {
    @Test(expected = MisconfigurationException.class)
    public void validationError() {
        ApplicationComponent component = new ApplicationComponent(TestApplicationFactory.class.getName());
        component.lifecycle().start(component);
    }
}