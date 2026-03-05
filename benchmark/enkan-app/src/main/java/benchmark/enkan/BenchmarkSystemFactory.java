package benchmark.enkan;

import enkan.component.ApplicationComponent;
import enkan.component.WebServerComponent;
import enkan.component.builtin.HmacEncoder;
import enkan.component.jackson.JacksonBeansConverter;
import enkan.component.undertow.UndertowComponent;
import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;

import static enkan.component.ComponentRelationship.component;
import static enkan.util.BeanBuilder.builder;

public class BenchmarkSystemFactory implements EnkanSystemFactory {
    @Override
    public EnkanSystem create() {
        return EnkanSystem.of(
                "hmac",    new HmacEncoder(),
                "jackson", new JacksonBeansConverter(),
                "app",     new ApplicationComponent<>("benchmark.enkan.BenchmarkApplicationFactory"),
                "http",    builder(new UndertowComponent())
                               .set(WebServerComponent::setPort, 8080)
                               .build()
        ).relationships(
                component("http").using("app"),
                component("app").using("jackson", "hmac")
        );
    }
}
