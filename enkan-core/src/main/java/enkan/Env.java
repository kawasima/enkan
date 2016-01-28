package enkan;

/**
 * @author kawasima
 */
public class Env {
    public static String getString(String name, String defaultValue) {
        String val = System.getenv(name);
        return val != null ? val : defaultValue;
    }

    public static int getInt(String name, int defaultValue) {
        String val = System.getenv(name);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }

    public static long getLong(String name, long defaultValue) {
        String val = System.getenv(name);
        return val != null ? Long.parseLong(val) : defaultValue;
    }

}
