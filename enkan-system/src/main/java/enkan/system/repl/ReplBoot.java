package enkan.system.repl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author kawasima
 */
public class ReplBoot {
    public static void start(String enkanSystemFactoryClassName) {
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(new PseudoRepl(enkanSystemFactoryClassName));
            service.shutdown();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}
