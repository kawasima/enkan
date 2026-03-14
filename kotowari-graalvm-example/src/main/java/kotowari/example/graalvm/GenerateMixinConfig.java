package kotowari.example.graalvm;

import enkan.application.WebApplication;
import enkan.util.MixinUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

/**
 * Standalone entry point invoked by the Maven {@code exec:java} goal (in the
 * {@code generate-resources} phase of the {@code native} profile) to
 * pre-generate the {@code $Mixin} class bytes required by GraalVM's
 * predefined-classes mechanism.
 *
 * <p>It triggers {@link MixinUtils#createFactory} by constructing the full
 * middleware stack via {@link NativeApplicationFactory#buildApp()} and calling
 * {@link WebApplication#createRequest()} once.  The resulting class bytes
 * (stored in {@link MixinUtils#generatedClassBytes}) are then written to
 * {@code target/predefined-classes/} and a matching
 * {@code target/predefined-classes-config.json} is produced.
 *
 * <p>Usage (via Maven exec plugin):
 * <pre>{@code
 * java -cp <classpath> kotowari.example.graalvm.GenerateMixinConfig <outputDir>
 * }</pre>
 * where {@code <outputDir>} defaults to {@code target} when not given.
 */
public class GenerateMixinConfig {

    public static void main(String[] args) throws Exception {
        Path outputDir = args.length > 0 ? Path.of(args[0]) : Path.of("target");

        // Build the app and trigger mixin class generation
        WebApplication app = NativeApplicationFactory.buildApp();
        app.createRequest();

        Map<String, byte[]> generatedBytes = MixinUtils.generatedClassBytes;
        if (generatedBytes.isEmpty()) {
            System.out.println("[GenerateMixinConfig] No mixin classes generated — nothing to write.");
            return;
        }

        // GraalVM expects the class bytes in agent-extracted-predefined-classes/<hash>.classdata
        // and the config to reference them by hash.
        Path predefinedDir = outputDir.resolve("agent-extracted-predefined-classes");
        Files.createDirectories(predefinedDir);

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        HexFormat hex = HexFormat.of();
        StringBuilder configJson = new StringBuilder("[\n");
        boolean first = true;

        for (Map.Entry<String, byte[]> entry : generatedBytes.entrySet()) {
            String className = entry.getKey();
            byte[] bytes = entry.getValue();

            String hash = hex.formatHex(sha256.digest(bytes));
            Path classFile = predefinedDir.resolve(hash + ".classdata");
            Files.write(classFile, bytes);

            if (!first) {
                configJson.append(",\n");
            }
            first = false;
            configJson.append("  {\n");
            configJson.append("    \"type\": \"agent-extracted\",\n");
            configJson.append("    \"classes\": [\n");
            configJson.append("      {\n");
            configJson.append("        \"hash\": \"").append(hash).append("\",\n");
            configJson.append("        \"nameInfo\": \"").append(className).append("\"\n");
            configJson.append("      }\n");
            configJson.append("    ]\n");
            configJson.append("  }");

            System.out.println("[GenerateMixinConfig] Wrote predefined class: " + className + " -> " + classFile);
        }
        configJson.append("\n]\n");

        Path configFile = outputDir.resolve("predefined-classes-config.json");
        Files.writeString(configFile, configJson.toString());
        System.out.println("[GenerateMixinConfig] Wrote predefined-classes-config.json: " + configFile);
    }
}
