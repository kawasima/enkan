package kotowari.routing.segment;

import enkan.collection.OptionMap;
import kotowari.routing.RegexpUtils;
import kotowari.routing.Segment;

/**
 * @author kawasima
 */
public class StaticSegment extends Segment {

    private boolean raw;

    public StaticSegment(String value) {
        this(value, OptionMap.of());
    }

    public StaticSegment(String value,  OptionMap options) {
        super(value);
        if (options.containsKey("raw")) {
            this.raw = options.getBoolean("raw");
        }

        if (options.containsKey("optional")) {
            setOptional(options.getBoolean("optional"));
        }
    }

    @Override
    public String interpolationChunk(OptionMap hash) {
        return raw ? getValue() : super.interpolationChunk(hash);
    }

    @Override
    public String regexpChunk() {
        String chunk = RegexpUtils.escape(getValue());
        return isOptional() ? RegexpUtils.optionalize(chunk) : chunk;
    }

    @Override
    public int numberOfCaptures() {
        return 0;
    }

    @Override
    public String buildPattern(String pattern) {
        String escaped = RegexpUtils.escape(getValue());
        if (isOptional() && !pattern.isEmpty()) {
            return "(?:" + RegexpUtils.optionalize(escaped) + "\\Z|" + escaped + RegexpUtils.unoptionalize(pattern) + ")";
        } else if (isOptional()) {
            return RegexpUtils.optionalize(escaped);
        } else {
            return escaped + pattern;
        }
    }

    @Override
    public String toString() {
        return getValue();
    }
}
