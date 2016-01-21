package kotowari.routing.segment;

import enkan.collection.OptionMap;
import kotowari.routing.RegexpUtils;
import kotowari.routing.Routes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author kawasima
 */
public class ControllerSegment extends DynamicSegment {
    public ControllerSegment(String value, OptionMap options) {
        super(value, options);
    }

    public ControllerSegment(String key) {
        super(key);
    }

    @Override
    public String regexpChunk() {
        /*
        List<String> possibleNames = new ArrayList<String>();
        for (String name : Routes.possibleControllers()) {
            possibleNames.add(RegexpUtils.escape(name));
        }
        return "(?i-:(" + String.join("|", possibleNames)+ "))";
        */
        return null;
    }

    @Override
    public void matchExtraction(OptionMap params, Matcher match, int nextCapture) {
        /*
        String key = getKey();
        String token = match.group(nextCapture);
        if (getDefault() != null) {
            params.put(key, !token.isEmpty() ? ControllerUtil.fromPathToClassName(token) : getDefault());
        } else {
            if (!token.isEmpty(token))
                params.put(key, token.fromPathToClassName(token));
        }
        */
    }

    @Override
    public String interpolationChunk(OptionMap hash) {
        String value = hash.getString(getKey());
        String path = value.replace(".", "/");
        if (path != null) {
            if (path.lastIndexOf('/') >= 0)
                path = substringBeforeLast(path, "/") + "/" + decapitalize(substringAfterLast(path, "/"));
            else
                path = decapitalize(path);
        }
        return path;
    }

    public static String decapitalize(String str) {
        if (str == null || str.isEmpty())
            return str;

        char cs[] = str.toCharArray();
        if (cs.length >=2 && Character.isUpperCase(cs[0])
                && Character.isUpperCase(cs[1]))
            return str;

        cs[0] = Character.toLowerCase(cs[0]);
        return new String(cs);
    }

    public static String substringBeforeLast(String str, String separator) {
        if (str.isEmpty() || separator.isEmpty()) {
            return str;
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    public static String substringAfterLast(String str, String separator) {
        if (str.isEmpty()) {
            return str;
        }
        if (separator.isEmpty()) {
            return "";
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1
                || pos == (str.length() - separator.length())) {
            return "";
        }
        return str.substring(pos + separator.length());
    }
}
