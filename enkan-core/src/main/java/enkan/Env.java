package enkan;

import enkan.exception.FalteringEnvironmentException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

/**
 * The utilities of environments.
 *
 * In the Enkan application, set environments apart from configurations.
 *
 * @author kawasima
 */
public class Env {
    static final Map<String, String> envMap = new HashMap<>();

    static {
        readEnvFile();
        readSystemEnv();
        readSystemProps();
    }

    /**
     * Normalize the given key to lower case delimited by dot..
     *
     * @param key The key of the environment variable.
     * @return a normalized String
     */
    private static String normalizeKey(String key) {
        return key.toLowerCase(Locale.US).replace('_', '.');
    }

    /**
     * Read environments from a file.
     */
    private static void readEnvFile() {
        Properties properties = new Properties();
        URL url = Thread.currentThread().getContextClassLoader().getResource("env.properties");
        if (url != null) {
            try (Reader reader=new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                properties.load(reader);
                properties.stringPropertyNames()
                        .stream()
                        .map(Env::normalizeKey)
                        .forEach(k -> envMap.put(k, properties.getProperty(k)));
            } catch (IOException e) {
                throw new FalteringEnvironmentException(e);
            }
        }
    }

    /**
     * Read environments from system properties.
     */
    private static void readSystemProps() {
        System.getProperties().stringPropertyNames()
                .stream()
                .map(Env::normalizeKey)
                .forEach(k -> envMap.put(k, System.getProperty(k)));
    }

    /**
     * Read environments from environment variables.
     */
    private static void readSystemEnv() {
        System.getenv().forEach((k, v) -> envMap.put(normalizeKey(k), v));
    }

    /**
     * Get a environment variable
     *
     * @param name variable name
     * @return value
     */
    public static String get(String name) {
        return getString(name, "");
    }

    /**
     * Get a environment variable as String.
     *
     * @param name variable name
     * @param defaultValue default value
     * @return value
     */
    public static String getString(String name, String defaultValue) {
        return Stream.of(name)
                .map(Env::normalizeKey)
                .map(envMap::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(defaultValue);
    }

    /**
     * Get a environment variable as Integer.
     *
     * @param name variable name
     * @param defaultValue default value
     * @return integer value
     */
    public static int getInt(String name, int defaultValue) {
        return Stream.of(name)
                .map(Env::normalizeKey)
                .map(envMap::get)
                .filter(Objects::nonNull)
                .findFirst()
                .map(Integer::parseInt)
                .orElse(defaultValue);
    }

    /**
     * Get a environment variable as Long.
     *
     * @param name variable name
     * @param defaultValue default value
     * @return long value
     */
    public static long getLong(String name, long defaultValue) {
        return Stream.of(name)
                .map(Env::normalizeKey)
                .map(envMap::get)
                .filter(Objects::nonNull)
                .findFirst()
                .map(Long::parseLong)
                .orElse(defaultValue);
    }

}
