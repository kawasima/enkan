package enkan.system.repl;

import enkan.system.Repl;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The ReplBoot class provides a method to start a REPL (Read-Eval-Print Loop) with optional system command registers.
 * It initializes a single-threaded executor service to run the REPL and registers any provided system commands.
 *
 * @author kawasima
 */
public class ReplBoot {
    public static void start(Repl repl, SystemCommandRegister... registers) {
        if (registers != null) {
            Arrays.stream(registers).forEach(r -> r.register(repl));
        }
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(repl);
        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
