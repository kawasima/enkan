package enkan.system.repl;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author kawasima
 */
public class ReplBoot {
    public static void start(String enkanSystemFactoryClassName, SystemCommandRegister... registers) {
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            PseudoRepl repl = new PseudoRepl(enkanSystemFactoryClassName);
            if (registers != null) {
                Arrays.stream(registers).forEach(r -> r.register(repl));
            }
            service.execute(repl);
            service.shutdown();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}
