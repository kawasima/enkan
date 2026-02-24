package kotowari.example;

import enkan.system.EnkanSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Production entry point.
 *
 * <p>Starts the Enkan system directly without REPL.</p>
 *
 * @author kawasima
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (System.getProperty("enkan.env") == null
                && System.getenv("ENKAN_ENV") == null) {
            System.setProperty("enkan.env", "production");
        }

        EnkanSystem system = new ExampleSystemFactory().create();
        system.start();
        LOG.info("Enkan system started.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            system.stop();
            LOG.info("Enkan system stopped.");
        }));
    }
}
