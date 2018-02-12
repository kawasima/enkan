package enkan.predicate;

import enkan.data.UriAvailable;

import java.util.regex.Pattern;

/**
 * @author kawasima
 */
public class PathPredicate<REQ extends UriAvailable> implements PrintablePredicate<REQ> {
    private String method;
    private Pattern pathPattern;

    protected PathPredicate(String method, String patternString) {
        this.method = method;
        pathPattern = Pattern.compile(patternString);
    }

    public static <REQ extends UriAvailable> PathPredicate<REQ> GET(String path) {
        return new PathPredicate<>("GET", path);
    }
    public static <REQ extends UriAvailable> PathPredicate<REQ> POST(String path) {
        return new PathPredicate<>("POST", path);
    }
    public static <REQ extends UriAvailable> PathPredicate<REQ> PUT(String path) {
        return new PathPredicate<>("PUT", path);
    }
    public static <REQ extends UriAvailable> PathPredicate<REQ> DELETE(String path) {
        return new PathPredicate<>("DELETE", path);
    }

    public static <REQ extends UriAvailable> PathPredicate<REQ> ANY(String path) {
        return new PathPredicate<>("ANY", path);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!method.equals("ANY")) {
            sb.append("method = ").append(method).append(" && ");
        }
        sb.append("path = ").append(pathPattern.pattern());

        return sb.toString();
    }
}
