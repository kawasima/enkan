package kotowari.example;

import enkan.system.EnkanSystem;
import enkan.collection.OptionMap;
import enkan.component.ApplicationComponent;
import enkan.component.JettyComponent;
import enkan.system.repl.PseudoRepl;
import enkan.system.repl.ReplBoot;
import kotowari.component.FreemarkerComponent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static enkan.system.ComponentRelationship.component;

/**
 * @author kawasima
 */
public class Main {
    public static void main(String[] args) {
        ReplBoot.start("kotowari.example.MyExampleSystemFactory");
    }
}
