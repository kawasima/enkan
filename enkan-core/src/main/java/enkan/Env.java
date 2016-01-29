package enkan;

import enkan.exception.FalteringEnvironmentException;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author kawasima
 */
public class Env {
    static Map<String, String> envMap = new HashMap<>();

    static {
        readEnvFile();
        readSystemEnv();
        readSystemProps();
    }

    private static void readEnvFile() {
        Properties properties = new Properties();
        URL url = Thread.currentThread().getContextClassLoader().getResource("application.properties");
        if (url != null) {
            try (Reader reader=new InputStreamReader(url.openStream(), "UTF-8")) {
                properties.load(reader);
                properties.stringPropertyNames().forEach(k -> envMap.put(k, System.getProperty(k)));
            } catch (IOException e) {
                throw FalteringEnvironmentException.create(e);
            }
        }
    }

    private static void readSystemProps() {
        System.getProperties().stringPropertyNames().forEach(k -> {
            envMap.put(k, System.getProperty(k));
        });
    }

    private static void readSystemEnv() {
        System.getenv().forEach((k, v) -> envMap.put(k, v));
    }

    public static String get(String name) {
        return getString(name, "");
    }

    public static String getString(String name, String defaultValue) {
        String val = envMap.get(name);
        return val != null ? val : defaultValue;
    }

    public static int getInt(String name, int defaultValue) {
        String val = envMap.get(name);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }

    public static long getLong(String name, long defaultValue) {
        String val = envMap.get(name);
        return val != null ? Long.parseLong(val) : defaultValue;
    }

}
