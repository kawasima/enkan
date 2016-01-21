package kotowari.example;

import enkan.system.EnkanSystem;
import enkan.collection.OptionMap;
import enkan.component.ApplicationComponent;
import enkan.component.JettyComponent;
import enkan.system.repl.PseudoRepl;
import kotowari.component.FreemarkerComponent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static enkan.system.ComponentRelationship.component;

/**
 * @author kawasima
 */
public class Main {
    public static void main(String[] args) {
        EnkanSystem system = EnkanSystem.of(
                "template", new FreemarkerComponent(),
                "app", new ApplicationComponent("kotowari.example.MyApplicationConfigurator"),
                "http", new JettyComponent(OptionMap.of("port", 3000))
        ).relationships(
                component("http").using("app"),
                component("app").using("template")
        );

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new PseudoRepl(system));
        service.shutdown();
    }
}
