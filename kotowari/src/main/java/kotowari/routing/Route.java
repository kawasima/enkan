package kotowari.routing;

import enkan.collection.OptionMap;
import enkan.util.CodecUtils;
import kotowari.routing.segment.DividerSegment;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kawasima
 */
public class Route {
    private List<Segment> segments;
    private OptionMap constraints;
    private OptionMap conditions;
    private List<String> significantKeys;
    private OptionMap parameterShell;
    private boolean matchingPrepared;
    private String controllerRequirement;
    private String actionRequirement;
    private Pattern recognizePattern;

    public Route(List<Segment> segments, OptionMap constraints, OptionMap conditions) {
        this.segments = segments;
        this.constraints = constraints;
        this.conditions = conditions;

        if (!significantKeys().contains("action") && !constraints.containsKey("action")) {
            constraints.put("action", "index");
            significantKeys().add("action");
        }
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public String buildQueryString(Map<String, String> hash) {
        List<String> elements = new ArrayList<String>();
        for(String key : hash.keySet()) {
            String value = hash.get(key);
            if (!value.isEmpty()) {
                elements.add(key + "=" + value);
            }
        }
        return String.join("&", elements);
    }

    public List<String> significantKeys() {
        if (significantKeys != null)
            return significantKeys;
        Set<String> sk = new HashSet<String>();
        for (Segment segment : segments) {
            if (segment.hasKey()) {
                sk.add(segment.getKey());
            }
        }
        sk.addAll(constraints.keySet());
        significantKeys = new ArrayList<String>(sk);
        return significantKeys;
    }

    @Override
    public String toString() {
        StringBuilder segs = new StringBuilder();
        for (Segment s : segments) {
            segs.append(s.toString());
        }
        List<Object> methods = conditions.getList("method");
        if (methods.isEmpty()) {
            methods.add("any");
        }
        StringBuilder out = new StringBuilder(256);
        for (Object method : methods) {
            out.append(String.format("%-6s %-40s %s\n", method.toString().toUpperCase(), segs.toString(), constraints));
        }
        return out.toString();
    }

    /*----recognize----*/
    public OptionMap recognize(String path, String method) {
        List<Object> methods = conditions.getList("method");
        if (!methods.isEmpty() && !methods.contains(method)) {
            return null;
        }
        if (recognizePattern == null) {
            recognizePattern = Pattern.compile(recognitionPattern(true));
        }
        Matcher match = recognizePattern.matcher(path);
        OptionMap params = null;
        if (match.find()) {
            int nextCapture = 1;
            params = OptionMap.of();
            params.putAll(getParameterShell());
            for (Segment segment : segments) {
                segment.matchExtraction(params, match, nextCapture);
                nextCapture += segment.numberOfCaptures();
            }
        }
        return params;
    }

    private String recognitionPattern(boolean wrap) {
        String pattern = "";
        for (int i = segments.size() - 1; i >= 0; i--) {
            Segment segment = segments.get(i);
            pattern = segment.buildPattern(pattern);
        }
        return wrap ? ("\\A" + pattern + "\\Z") : pattern;
    }

    private String getUrlEncodedString(OptionMap options, String key) {
        Object value = options.get(key);
        if (value == null) {
            return "";
        } else if (value instanceof Collection) {
            Collection<?> values = Collection.class.cast(value);
            List<String> pairs = new ArrayList<String>(values.size());
            for (Object val : values) {
                if (val == null)
                    val = "";
                pairs.add(CodecUtils.urlEncode(key) +  "=" + CodecUtils.urlEncode(val.toString()));
            }
            return String.join("&", pairs);
        } else {
            return CodecUtils.urlEncode(key) + "=" + CodecUtils.urlEncode(value.toString());
        }
    }

    private String buildQueryString(OptionMap hash, List<String> onlyKeys) {
        List<String> elements = new ArrayList<String>(hash.size());

        if (onlyKeys == null)
            onlyKeys = new ArrayList<String>(hash.keySet());

        for (String key : onlyKeys) {
            if (hash.containsKey(key)) {
                elements.add(getUrlEncodedString(hash, key));
            }
        }
        return elements.isEmpty() ? "" : "?" + String.join("&", elements);
    }

    private OptionMap getParameterShell() {
        if (parameterShell == null) {
            OptionMap options = OptionMap.of();
            for (Map.Entry<String, Object> e : constraints.entrySet()) {
                if (! (e.getValue() instanceof Pattern)) {
                    options.put(e.getKey(), e.getValue());
                }
            }
            parameterShell = options;
        }
        return parameterShell;

    }

    /* --- generate --- */
    public boolean matchesControllerAndAction(String controller, String action) {
        prepareMatching();
        return  (controllerRequirement == null || controller.equals(controllerRequirement)) &&
                (actionRequirement == null || action.equals(actionRequirement));
    }

    public String generate(OptionMap options, OptionMap hash) {
        if (generationRequirements(options, hash)) {
            int lastIndex = segments.size() - 1;
            Segment last = segments.get(lastIndex);
            String path = last.stringStructure(segments.subList(0, lastIndex), hash);
            if (segments.size() > 1 && last instanceof DividerSegment && "/".equals(last.getValue())) {
                path = path + "/";
            }
            return appendQueryString(path, hash, extraKeys(options));
        }
        return null;
    }

    public boolean generationRequirements(OptionMap options, OptionMap hash) {
        boolean matched = true;
        for(String key : constraints.keySet()) {
            Object req = constraints.get(key);
            if (req instanceof Pattern) {
                matched &= (hash.containsKey(key) && ((Pattern)req).matcher(options.getString(key)).matches());
            } else {
                matched &= hash.getString(key).equals(constraints.getString(key));
            }
        }
        return matched;
    }
    private String requirementFor(String key) {
        if (constraints.containsKey(key))
            return constraints.getString(key);
        for (Segment segment : segments) {
            if (segment.hasKey() && segment.getKey().equals(key)) {
                return segment.getRegexp();
            }
        }
        return null;
    }
    private void prepareMatching() {
        if (!matchingPrepared) {
            controllerRequirement = requirementFor("controller");
            actionRequirement = requirementFor("action");
            matchingPrepared = true;
        }
    }

    private String appendQueryString(String path, OptionMap hash, List<String> queryKeys) {
        if (path == null)
            return null;

        if (queryKeys == null)
            queryKeys = extraKeys(hash);

        return path + buildQueryString(hash, queryKeys);
    }

    private List<String> extraKeys(OptionMap hash) {
        List<String> extraKeys = new ArrayList<String>();
        if (hash != null) {
            for (String key : hash.keySet()) {
                if (!significantKeys.contains(key))
                    extraKeys.add(key);
            }
        }
        return extraKeys;
    }
}
