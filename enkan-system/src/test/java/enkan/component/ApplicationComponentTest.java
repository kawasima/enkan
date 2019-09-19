package enkan.component;

import enkan.MiddlewareChain;
import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
class ApplicationComponentTest {
    @Test
    void validationError() {
        assertThatThrownBy(() -> {
            ApplicationComponent component = new ApplicationComponent(TestApplicationFactory.class.getName());
            component.lifecycle().start(component);
        }).isExactlyInstanceOf(MisconfigurationException.class);
    }

    @Test
    void applicationCustomizer() {
        ApplicationComponent<String, String> component = new ApplicationComponent<>(TestCorrectApplicationFactory.class.getName());
        component.setApplicationCustomizer(app -> {
            app.getMiddlewareStack().stream()
                    .map(MiddlewareChain::getMiddleware)
                    .filter(Test3Middleware.class::isInstance)
                    .map(Test3Middleware.class::cast)
                    .forEach(m -> m.setAdditionalMessage("Hello "));
            return app;
        });
        component.lifecycle().start(component);
        assertThat(component.getApplication().handle("world"))
                .isEqualTo("Hello world");
    }
}
