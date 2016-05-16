package enkan.component.undertow;

import enkan.exception.MisconfigurationException;
import org.junit.Test;

import static enkan.util.BeanBuilder.builder;
import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class UndertowComponentTest {
    @Test(expected = MisconfigurationException.class)
    public void parameterValidation() {
        builder(new UndertowComponent())
                .set(UndertowComponent::setPort, 77777)
                .build();
    }
}
