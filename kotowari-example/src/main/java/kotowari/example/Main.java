package kotowari.example;

import enkan.system.devel.DevelCommandRegister;
import enkan.system.repl.ReplBoot;
import kotowari.system.KotowariCommandRegister;

/**
 * @author kawasima
 */
public class Main {
    public static void main(String[] args) {
        ReplBoot.start("kotowari.example.MyExampleSystemFactory",
                new KotowariCommandRegister(),
                new DevelCommandRegister());
    }
}
