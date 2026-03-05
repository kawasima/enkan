package enkan.system.repl;

import enkan.system.Repl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Builder for configuring and starting a REPL server.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * new ReplBoot(repl)
 *     .register(new KotowariCommandRegister())
 *     .register(r -> r.registerCommand("sql", new SqlCommand()))
 *     .onReady("/start")
 *     .start();
 * }</pre>
 *
 * @author kawasima
 */
public class ReplBoot {
    private final Repl repl;
    private final List<SystemCommandRegister> registers = new ArrayList<>();
    private final List<TransportProvider> transportProviders = new ArrayList<>();
    private final List<String> initialCommands = new ArrayList<>();

    public ReplBoot(Repl repl) {
        this.repl = repl;
    }

    /**
     * Register a command register that will be applied before the REPL starts.
     *
     * @param register a command register
     * @return this builder
     */
    public ReplBoot register(SystemCommandRegister register) {
        registers.add(register);
        return this;
    }

    /**
     * Add a transport provider that will be started when the REPL runs.
     *
     * @param provider a transport provider
     * @return this builder
     */
    public ReplBoot transport(TransportProvider provider) {
        transportProviders.add(provider);
        return this;
    }

    /**
     * Specify commands to execute automatically after the REPL becomes ready.
     *
     * @param commands command strings (e.g. "/start", "/autoreset")
     * @return this builder
     */
    public ReplBoot onReady(String... commands) {
        initialCommands.addAll(Arrays.asList(commands));
        return this;
    }

    /**
     * Start the REPL. This method blocks until the REPL shuts down.
     */
    public void start() {
        registers.forEach(r -> r.register(repl));
        transportProviders.forEach(repl::addTransportProvider);

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(repl);

        if (!initialCommands.isEmpty()) {
            Thread starter = new Thread(() -> {
                repl.getPort(); // blocks until REPL is ready
                initialCommands.forEach(repl::execute);
            });
            starter.setDaemon(true);
            starter.start();
        }

        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Convenience static method for backwards compatibility.
     *
     * @param repl      the REPL to start
     * @param registers optional command registers
     */
    public static void start(Repl repl, SystemCommandRegister... registers) {
        ReplBoot boot = new ReplBoot(repl);
        if (registers != null) {
            Arrays.stream(registers).forEach(boot::register);
        }
        boot.start();
    }
}
