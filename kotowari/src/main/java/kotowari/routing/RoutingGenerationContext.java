package kotowari.routing;

import enkan.collection.OptionMap;

/**
 * @author kawasima
 */
public class RoutingGenerationContext {
    private OptionMap options;

    public RoutingGenerationContext(OptionMap options) {
        this.options = options;
    }

    public OptionMap getOptions() {
        return options;
    }
}
