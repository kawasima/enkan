package kotowari.routing.factory;

import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.util.HttpRequestUtils;
import kotowari.routing.Route;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Representation for conditions of routing.
 *
 * @author kawasima
 */
public class RoutingCondition {
    private String method;
    private String path;
    private OptionMap requirements;
    private Set<MediaType> consumes;
    private Set<MediaType> produces;
    private RoutePatterns.PatternsContext context;

    public RoutingCondition(String method, String path) {
        this.method = method;
        this.path = path;
        this.requirements = OptionMap.empty();
        this.consumes = Collections.emptySet();
        this.produces = Collections.emptySet();
    }

    public void setContext(RoutePatterns.PatternsContext context) {
        this.context = context;
    }

    public RoutingCondition requires(String patternVariable, String pattern) {
        requirements.put(patternVariable, Pattern.compile(pattern));
        return this;
    }

    public RoutingCondition consumes(MediaType... mediaTypes) {
        this.consumes = Arrays.stream(mediaTypes)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return this;
    }

    public RoutingCondition consumes(String... mediaTypes) {
        this.consumes = Arrays.stream(mediaTypes)
                .filter(Objects::nonNull)
                .map(MediaType::valueOf)
                .collect(Collectors.toSet());
        return this;
    }

    public RoutingCondition produces(MediaType... mediaTypes) {
        this.produces = Arrays.stream(mediaTypes)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return this;
    }

    public RoutingCondition produces(String... mediaTypes) {
        this.produces = Arrays.stream(mediaTypes)
                .filter(Objects::nonNull)
                .map(MediaType::valueOf)
                .collect(Collectors.toSet());
        return this;
    }

    /**
     * Generate a routing definition from the given condition and destination
     *
     * @param controllerClass  The controller of destination
     * @param controllerMethod The method of destination
     * @return a routing definition
     */
    public Route to(Class<?> controllerClass, String controllerMethod) {
        OptionMap conditions = OptionMap.of("method", method);
        OptionMap options = OptionMap.of(
                "controller", controllerClass,
                "action", controllerMethod,
                "conditions", conditions);

        if (!requirements.isEmpty()) {
            options.put("requirements", requirements);
        }
        if (!consumes.isEmpty()) {
            options.put("consumes", consumes);
        }
        if (!produces.isEmpty()) {
            options.put("produces", produces);
        }

        Route route = context.build(path, options);
        context.addRoute(route);
        return route;
    }
}
