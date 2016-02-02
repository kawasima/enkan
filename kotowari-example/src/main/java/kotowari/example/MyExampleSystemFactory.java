package kotowari.example;

import enkan.Env;
import enkan.collection.OptionMap;
import enkan.component.*;
import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;
import kotowari.component.FreemarkerComponent;

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
                "flyway", new FlywayMigration(),
                "template", new FreemarkerComponent(),
                "datasource", new HikariCPComponent(OptionMap.of("uri", "jdbc:h2:mem:test")),
                "app", new ApplicationComponent("kotowari.example.MyApplicationFactory"),
                "http", builder(new JettyComponent())
                        .set(JettyComponent::setPort, Env.getInt("PORT", 3000))
                        .build()
        ).relationships(
                component("http").using("app"),
                component("app").using("template", "doma", "datasource"),
                component("doma").using("datasource"),
                component("flyway").using("datasource")
        );

    }

}
