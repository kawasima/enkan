package kotowari.example;

import enkan.collection.OptionMap;
import enkan.component.ApplicationComponent;
import enkan.component.JettyComponent;
import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;
import kotowari.component.FreemarkerComponent;

import static enkan.system.ComponentRelationship.component;

/**
 * @author kawasima
 */
public class MyExampleSystemFactory implements EnkanSystemFactory {
    @Override
    public EnkanSystem create() {
        return EnkanSystem.of(
                "template", new FreemarkerComponent(),
                "app", new ApplicationComponent(MyApplicationConfigurator.class),
                "http", new JettyComponent(OptionMap.of("port", 3000))
        ).relationships(
                component("http").using("app"),
                component("app").using("template")
        );

    }

}
