package enkan.decision;

import enkan.Decision;
import enkan.data.UriAvailable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * @author kawasima
 */
public class PathDecision<REQ extends UriAvailable> implements Decision<REQ> {
    private static final Logger LOG = LoggerFactory.getLogger("enkan.decision");

    private String method;
    private Pattern pathPattern;

    protected PathDecision(String method, String patternString) {
        this.method = method;
        pathPattern = Pattern.compile(patternString);
    }

    public static PathDecision GET(String path) {
        return new PathDecision("GET", path);
    }
    public static PathDecision POST(String path) {
        return new PathDecision("POST", path);
    }
    public static PathDecision PUT(String path) {
        return new PathDecision("PUT", path);
    }
    public static PathDecision DELETE(String path) {
        return new PathDecision("DELETE", path);
    }

    @Override
    public boolean decide(REQ req) {
        String path = req.getUrl();
        return path != null && pathPattern.matcher(path).matches();
    }
}
