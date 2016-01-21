package kotowari.routing.segment;

import enkan.collection.OptionMap;

/**
 * @author kawasima
 */
public class DividerSegment extends StaticSegment {
    public DividerSegment(String value, OptionMap options) {
        super(value, setDefault(options));
    }

    private static OptionMap setDefault(OptionMap options) {
        options.put("raw", true);
        options.put("optional", true);
        return options;
    }


    public DividerSegment(String value) {
        this(value, OptionMap.of());
    }
    public DividerSegment() {
        this(null, OptionMap.of());
    }

    public boolean isOptionalityImplied() {
        return true;
    }
}
