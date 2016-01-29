package kotowari.example;

import enkan.Env;
import enkan.collection.OptionMap;
import enkan.component.*;
import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;
import kotowari.component.FreemarkerComponent;

import static enkan.component.ComponentRelationship.component;

/**
 * @author kawasima
 */
public class MyExampleSystemFactory implements EnkanSystemFactory {
    @Override
    public EnkanSystem create() {
        return EnkanSystem.of(
                "doma", new DomaDaoProvider(),
                "flyway", new FlywayMigration(),
                "template", new FreemarkerComponent(),
                "datasource", new HikariCPComponent(OptionMap.of("uri", "jdbc:h2:mem:test")),
                "app", new ApplicationComponent("kotowari.example.MyApplicationFactory"),
                "http", new JettyComponent(OptionMap.of("port", Env.getInt("PORT", 3000)))
        ).relationships(
                component("http").using("app"),
                component("app").using("template", "doma"),
                component("doma").using("datasource"),
                component("flyway").using("datasource")
        );

    }

}
