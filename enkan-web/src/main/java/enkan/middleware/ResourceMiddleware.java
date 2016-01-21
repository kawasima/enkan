package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import java.util.HashSet;
import java.util.Set;

import static enkan.util.HttpRequestUtils.*;
import static enkan.util.HttpResponseUtils.*;
import static enkan.util.CodecUtils.*;

/**
 * @author kawasima
 */
@Middleware(name = "resource")
public class ResourceMiddleware extends AbstractWebMiddleware {
    private String rootPath;
    private static final Set<String> ACCEPTABLE_METHODS = new HashSet<String>() {{
        add("get");
        add("head");
    }};

    public ResourceMiddleware(String rootPath) {
        this.rootPath = rootPath;
    }

    protected HttpResponse resourceRequest(HttpRequest request, String rootPath) {
        if (ACCEPTABLE_METHODS.contains(request.getRequestMethod())) {
            String path = urlDecode(pathInfo(request)).substring(1);
            if (path != null) {
                return resourceResponse(path, OptionMap.of("root", rootPath));
            }
        }

        return null;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest, MiddlewareChain next) {
        HttpResponse response = resourceRequest(httpRequest, rootPath);
        if (response == null) {
            response = (HttpResponse) next.next(response);
        }
        return response;
    }
}
