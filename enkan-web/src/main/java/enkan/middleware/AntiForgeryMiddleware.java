package enkan.middleware;


import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Session;
import enkan.data.SessionAvailable;
import enkan.util.ThreadingUtils;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.multimap.Multimap;

import java.util.Optional;
import java.util.UUID;

/**
 * @author kawasima
 */
@Middleware(name = "antiForgery", dependencies = {"session"})
public class AntiForgeryMiddleware extends AbstractWebMiddleware {
    private String newToken() {
        return UUID.randomUUID().toString();
    }

    private Optional<String> sessionToken(HttpRequest request) {
        return ThreadingUtils.some(request,
                HttpRequest::getSession,
                s -> s.getAttribute("antiForgeryToken"),
                String::toString);
    }

    private Multimap<String, String> formParams(HttpRequest request) {
        return request.getParams();
    }

    private Optional<String> defaultRequestToken(HttpRequest request) {
        return ThreadingUtils.some(request, this::formParams, p -> p.get("__anti-forgery-token"), RichIterable::getFirst);
    }

    private boolean isValidRequest(HttpRequest request) {
        Optional<String> readToken = defaultRequestToken(request);
        Optional<String> storedToken = sessionToken(request);
        return readToken.isPresent() && storedToken.isPresent() && readToken.get().equals(storedToken.get());
    }

    private boolean isGetRequest(HttpRequest request) {
        String method = request.getRequestMethod();
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method);
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        String antiForgeryToken = sessionToken(request).orElseGet(this::newToken);
        if (!isGetRequest(request) && !isValidRequest(request)) {
            return HttpResponse.of("<h1>Invalid anti-forgery token</h1>");
        } else {
            HttpResponse response = castToHttpResponse(next.next(request));
            return response;
        }
    }
}
