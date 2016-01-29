package enkan.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kawasima
 */
public class MimeTypeUtils {
    static final Map<String, String> DEFAULT_MIME_TYPES = new HashMap<String, String>() {{
        put("7z",       "application/x-7z-compressed");
        put("css",      "text/css");
        put("csv",      "text/csv");
        put("js",       "text/javascript");
        put("json",     "application/javascript");
        put("htm",      "text/html");
        put("html",     "text/html");
    }};

    private static String filenameExt(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx == -1 || idx == filename.length()) {
            return null;
        } else {
            return filename.substring(idx + 1);
        }

    }

    public static String extMimeType(String filename) {
        String ext = filenameExt(filename);
        return ext != null ? DEFAULT_MIME_TYPES.get(ext) : null;
    }
}
