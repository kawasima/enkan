package enkan;

import enkan.exception.FalteringEnvironmentException;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The utilities of environments.
 *
 * In the Enkan application, set environments apart from configurations.
 *
 * @author kawasima
 */
public class Env {
    static Map<String, String> envMap = new HashMap<>();

    static {
        readEnvFile();
        readSystemEnv();
        readSystemProps();
    }

    /**
     * Read environments from a file.
     */
    private static void readEnvFile() {
        Properties properties = new Properties();
        URL url = Thread.currentThread().getContextClassLoader().getResource("env.properties");
        if (url != null) {
            try (Reader reader=new InputStreamReader(url.openStream(), "UTF-8")) {
                properties.load(reader);
                properties.stringPropertyNames().forEach(k -> envMap.put(k, System.getProperty(k)));
            } catch (IOException e) {
                throw FalteringEnvironmentException.create(e);
            }
        }
    }

    /**
     * Read environments from system properties.
     */
    private static void readSystemProps() {
        System.getProperties().stringPropertyNames()
                .forEach(k -> envMap.put(k, System.getProperty(k)));
    }

    /**
     * Read environments from environment variables.
     */
    private static void readSystemEnv() {
        System.getenv().forEach((k, v) -> envMap.put(k, v));
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
        String val = envMap.get(name);
        return val != null ? val : defaultValue;
    }

    /**
     * Get a environment variable as Integer.
     *
     * @param name variable name
     * @param defaultValue default value
     * @return integer value
     */
    public static int getInt(String name, int defaultValue) {
        String val = envMap.get(name);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }

    /**
     * Get a environment variable as Long.
     *
     * @param name variable name
     * @param defaultValue default value
     * @return long value
     */
    public static long getLong(String name, long defaultValue) {
        String val = envMap.get(name);
        return val != null ? Long.parseLong(val) : defaultValue;
    }

}
