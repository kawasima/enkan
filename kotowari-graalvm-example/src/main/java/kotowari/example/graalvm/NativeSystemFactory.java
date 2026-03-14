package kotowari.example.graalvm;

import enkan.component.ApplicationComponent;
import enkan.component.WebServerComponent;
import enkan.component.jackson.JacksonBeansConverter;
import enkan.component.undertow.UndertowComponent;
import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;

import static enkan.component.ComponentRelationship.component;
import static enkan.util.BeanBuilder.builder;

public class NativeSystemFactory implements EnkanSystemFactory {
    @Override
    public EnkanSystem create() {
        return EnkanSystem.of(
            "jackson", new JacksonBeansConverter(),
            "app", new ApplicationComponent<>("kotowari.example.graalvm.NativeApplicationFactory"),
            "http", builder(new UndertowComponent())
                .set(WebServerComponent::setPort, 3000)
                .build()
        ).relationships(
            component("http").using("app"),
            component("app").using("jackson")
        );
    }
}
