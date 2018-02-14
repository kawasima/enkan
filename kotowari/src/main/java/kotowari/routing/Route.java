package kotowari.routing;

import enkan.collection.OptionMap;
import enkan.data.ContentNegotiable;
import enkan.data.HttpRequest;
import enkan.util.CodecUtils;
import enkan.util.HttpRequestUtils;
import kotowari.routing.segment.DividerSegment;

import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static enkan.util.ThreadingUtils.some;

/**
 * Represents a routing information.
 *
 * @author kawasima
 */
public class Route {
    /** Path segments */
    private List<Segment> segments;

    private OptionMap constraints;
    private OptionMap conditions;
    private List<String> significantKeys;
    private OptionMap parameterShell;
    private boolean matchingPrepared;
    private Class<?> controllerRequirement;
    private String actionRequirement;
    private Pattern recognizePattern;

    /**
     * Constructs an instance of a routing given its path segments, constraints and conditions.
     *
     * @param segments    path segments
     * @param constraints constraints
     * @param conditions  conditions
     */
    public Route(List<Segment> segments, OptionMap constraints, OptionMap conditions) {
        this.segments = segments;
        this.constraints = constraints;
        this.conditions = conditions;

        if (!significantKeys().contains("action") && !constraints.containsKey("action")) {
            constraints.put("action", "index");
            significantKeys().add("action");
        }
    }

    /**
     * Returns path segments.
     *
     * @return path segments
     */
    public List<Segment> getSegments() {
        return segments;
    }

    public String buildQueryString(Map<String, String> hash) {
        List<String> elements = new ArrayList<>();
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
        Set<String> sk = segments.stream()
                .filter(Segment::hasKey)
                .map(Segment::getKey)
                .collect(Collectors.toSet());
        sk.addAll(constraints.keySet());
        significantKeys = new ArrayList<>(sk);
        return significantKeys;
    }

    /**
     * Recognize routing from HttpRequest.
     *
     * @param request a http request
     * @return recognized routing information
     */
    @SuppressWarnings("unchecked")
    public OptionMap recognize(HttpRequest request) {
        Set<MediaType> produces = (Set<MediaType>) conditions.get("produces");
        if (produces != null && request instanceof ContentNegotiable) {
            MediaType produceType = ((ContentNegotiable) request).getMediaType();
            if (produces.stream().noneMatch(produceType::isCompatible)) {
                return null;
            }
        }

        Set<MediaType> consumes = (Set<MediaType>) conditions.get("consumes");
        if (consumes != null) {
            MediaType consumeType = some(HttpRequestUtils.contentType(request),
                    type -> type.split("/", 2),
                    types -> new MediaType(types[0], types[1])).orElse(null);
            if (consumeType == null || consumes.stream().noneMatch(consumeType::isCompatible)) {
                return null;
            }
        }

        return recognize(request.getUri(), request.getRequestMethod().toUpperCase(Locale.ENGLISH));
    }

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
            List<String> pairs = new ArrayList<>(values.size());
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
        List<String> elements = new ArrayList<>(hash.size());

        if (onlyKeys == null)
            onlyKeys = new ArrayList<>(hash.keySet());

        elements.addAll(onlyKeys.stream()
                .filter(hash::containsKey)
                .map(key -> getUrlEncodedString(hash, key))
                .collect(Collectors.toList()));
        return elements.isEmpty() ? "" : "?" + String.join("&", elements);
    }

    private OptionMap getParameterShell() {
        if (parameterShell == null) {
            OptionMap options = OptionMap.of();
            constraints.entrySet().stream()
                    .filter(e -> !(e.getValue() instanceof Pattern))
                    .forEach(e -> options.put(e.getKey(), e.getValue()));
            parameterShell = options;
        }
        return parameterShell;

    }

    /* --- generate --- */
    public boolean matchesController(Class<?> controller) {
        prepareMatching();
        return controllerRequirement != null && controller.equals(controllerRequirement);
    }

    public boolean matchesControllerAndAction(Class<?> controller, String action) {
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
            controllerRequirement = (Class<?>) constraints.get("controller");
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
        List<String> extraKeys = new ArrayList<>();
        if (hash != null) {
            extraKeys.addAll(hash.keySet().stream()
                    .filter(key -> !significantKeys.contains(key))
                    .collect(Collectors.toList()));
        }
        return extraKeys;
    }

    public String getActionRequirement() {
        return actionRequirement;
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
            out.append(String.format(Locale.US, "%-6s %-40s %s\n", method.toString().toUpperCase(), segs.toString(), constraints));
        }
        return out.toString();
    }

}
