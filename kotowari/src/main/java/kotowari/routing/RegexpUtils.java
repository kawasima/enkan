package kotowari.routing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kawasima
 */
public class RegexpUtils {
    private static final Pattern[] UNOPTIONALIZED_PTNS = {
            Pattern.compile("\\A\\(\\?:(.*)\\)\\?\\Z"),
            Pattern.compile("\\A(.|\\(.*\\))\\?\\Z")
    };
    public static String escape(String pattern) {
        return pattern.replaceAll("([\\.\\-\\[\\]])", "\\\\$1");
    }

    public static String optionalize(String pattern) {
        String unoptionalizedPattern = unoptionalize(pattern);
        if (Pattern.matches("\\A(.|\\(.*\\))\\Z", unoptionalizedPattern)) {
            return pattern + "?";
        } else {
            return "(?:" + pattern + ")?";
        }
    }
    public static String unoptionalize(String pattern) {
        for (Pattern regexp : UNOPTIONALIZED_PTNS) {
            Matcher m = regexp.matcher(pattern);
            if (m.find()) {
                return m.group(1);
            }
        }
        return pattern;
    }
}
