package enkan.system.repl;

import enkan.system.loader.EnkanLoader;

import java.net.URLClassLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author kawasima
 */
public class ReplBoot {
    public static void start(String enkanSystemFactoryClassName) {
        EnkanLoader loader = new EnkanLoader((URLClassLoader) ReplBoot.class.getClassLoader());
        try {
            ExecutorService service = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setContextClassLoader(loader);
                return t;
            });
            Class replClass = loader.loadClass("enkan.system.repl.PseudoRepl");
            service.execute((Runnable) replClass.getDeclaredConstructor(String.class).newInstance(enkanSystemFactoryClassName));
            service.shutdown();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
