package enkan.system.repl.jshell;

import enkan.system.EnkanSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ImportUtils {
    public static void importWellKnownClasses(EnkanSystem system) {
        system.getAllComponents().stream()
                .map(component -> component.getClass().getName());
        URL url = ClassLoader.getSystemResource("rootpackage");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            reader.readLine();
        } catch (IOException e) {

        }
    }
}
