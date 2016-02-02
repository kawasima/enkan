package enkan.predicate;

import enkan.data.UriAvailable;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author kawasima
 */
public class PathPredicate<REQ extends UriAvailable> implements Predicate<REQ> {
    private String method;
    private Pattern pathPattern;

    protected PathPredicate(String method, String patternString) {
        this.method = method;
        pathPattern = Pattern.compile(patternString);
    }

    public static PathPredicate GET(String path) {
        return new PathPredicate("GET", path);
    }
    public static PathPredicate POST(String path) {
        return new PathPredicate("POST", path);
    }
    public static PathPredicate PUT(String path) {
        return new PathPredicate("PUT", path);
    }
    public static PathPredicate DELETE(String path) {
        return new PathPredicate("DELETE", path);
    }

    public static PathPredicate ANY(String path) {
        return new PathPredicate("ANY", path);
    }

    @Override
    public boolean test(REQ request) {
        if (request != null) {
            String path = request.getUri();
            String requestMethod = request.getRequestMethod();
            return path != null && pathPattern.matcher(path).matches()
                    && requestMethod != null
                    && (requestMethod.equalsIgnoreCase(method) || "ANY".equalsIgnoreCase(method));
        } else {
            return false;
        }
    }
}
