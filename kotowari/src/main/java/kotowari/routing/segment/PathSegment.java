package kotowari.routing.segment;

import enkan.collection.OptionMap;
import enkan.exception.UnrecoverableException;

import java.util.regex.Matcher;

/**
 * @author kawasima
 */
public class PathSegment extends DynamicSegment {
    public PathSegment(String key, OptionMap options) {
        super(key, options);
    }

    @Override
    public String interpolationChunk(OptionMap hash) {
        return hash.getString(getKey());
    }

    @Override
    public String getDefault() {
        return "";
    }

    public void setDefault(String path) {
        if (!path.isEmpty())
            throw UnrecoverableException.create("paths cannot have non-empty default values"); // TODO what is this?
    }

    public String defaultRegexpChunk() {
        return "(.*)";
    }

    @Override
    public int numberOfCaptures() {
        return 1;
    }

    public boolean optionalityImplied() {
        return true;
    }

    @Override
    public void matchExtraction(OptionMap params, Matcher match, int nextCapture) {
        String value = match.group(nextCapture);
        params.put(getKey(), value);
    }
}
