package enkan.docs;

import org.jbake.app.Oven;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;

import java.io.File;

public class BakeDocsSite {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: BakeDocsSite <source-directory> <destination-directory>");
            System.exit(1);
        }

        File source = new File(args[0]);
        File destination = new File(args[1]);

        DefaultJBakeConfiguration config = new JBakeConfigurationFactory()
                .createDefaultJbakeConfiguration(source, destination, true);

        Oven oven = new Oven(config);
        oven.bake();

        if (!oven.getErrors().isEmpty()) {
            for (Throwable error : oven.getErrors()) {
                System.err.println(error.getMessage());
            }
            System.exit(1);
        }
    }
}
