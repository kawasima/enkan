package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static enkan.util.CodecUtils.urlDecode;
import static enkan.util.HttpRequestUtils.pathInfo;
import static enkan.util.HttpResponseUtils.resourceResponse;

/**
 * @author kawasima
 */
@Middleware(name = "resource")
public class ResourceMiddleware extends AbstractWebMiddleware {
    private String rootPath = "public";
    private String uriPrefix = "assets/";

    private static final Set<String> ACCEPTABLE_METHODS = new HashSet<String>() {{
        add("GET");
        add("HEAD");
    }};

    protected HttpResponse resourceRequest(HttpRequest request, String rootPath) {
        if (ACCEPTABLE_METHODS.contains(
                Objects.toString(request.getRequestMethod(), "").toUpperCase(Locale.US))) {
            String path = urlDecode(pathInfo(request)).substring(1);
            if (!path.startsWith(uriPrefix)) {
                return null;
            }
            int len = uriPrefix.length();
            if (len > 0) path = path.substring(len);
            return resourceResponse(path, OptionMap.of("root", rootPath));
        }

        return null;
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
        HttpResponse response = resourceRequest(request, rootPath);
        if (response == null) {
            response = (HttpResponse) chain.next(request);
        }
        return response;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void setUriPrefix(String uriPrefix) {
        if (uriPrefix == null) {
            this.uriPrefix = "";
        } else {
            if (uriPrefix.startsWith("/")) uriPrefix = uriPrefix.substring(1);
            if (!uriPrefix.endsWith("/")) uriPrefix += "/";
            this.uriPrefix = uriPrefix;
        }
    }
}
