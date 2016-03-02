package kotowari.example;

import enkan.Env;
import enkan.collection.OptionMap;
import enkan.component.*;
import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;

import static enkan.component.ComponentRelationship.component;
import static enkan.util.BeanBuilder.builder;

/**
 * @author kawasima
 */
public class MyExampleSystemFactory implements EnkanSystemFactory {
    @Override
    public EnkanSystem create() {
        return EnkanSystem.of(
                "doma", new DomaProvider(),
                "jackson", new JacksonBeansConverter(),
                "flyway", new FlywayMigration(),
                "template", new FreemarkerTemplateEngine(),
                "metrics", new MetricsComponent(),
                "datasource", new HikariCPComponent(OptionMap.of("uri", "jdbc:h2:mem:test")),
                "app", new ApplicationComponent("kotowari.example.MyApplicationFactory"),
                "http", builder(new UndertowComponent())
                        .set(UndertowComponent::setPort, Env.getInt("PORT", 3000))
                        .build()
        ).relationships(
                component("http").using("app"),
                component("app").using("datasource", "template", "doma", "jackson", "metrics"),
                component("doma").using("datasource", "flyway"),
                component("flyway").using("datasource")
        );

    }

}
