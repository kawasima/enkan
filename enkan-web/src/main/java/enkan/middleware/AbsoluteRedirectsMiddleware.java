package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.util.ThreadingUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static enkan.util.HttpRequestUtils.requestUrl;

/**
 * @author kawasima
 */
@Middleware(name = "absoluteRedirect")
public class AbsoluteRedirectsMiddleware implements WebMiddleware {
    private static final Set<Integer> REDIRECT_STATUS = new HashSet<>(Arrays.asList(201, 301, 302, 303, 307));

    protected boolean isRedirectResponse(HttpResponse response) {
        return REDIRECT_STATUS.contains(response.getStatus());
    }

    protected void updateHeader(HttpResponse response, String header, HttpRequest request) {
        ThreadingUtils.some(response.getHeaders().get(header), Object::toString)
                .ifPresent(url -> response.getHeaders().replace(header, absoluteUrl(url, request)));
    }

    protected boolean isUrl(String s) {
        try {
            URL url = URI.create(s).toURL();
            return true;
        } catch (IllegalArgumentException | MalformedURLException e) {
            return false;
        }
    }

    protected String absoluteUrl(String location, HttpRequest request) {
        if (isUrl(location)) {
            return location;
        } else {
            try {
                URL url = URI.create(requestUrl(request)).toURL();
                return new URL(url, location).toString();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(
                        String.format("wrong location %s or request url %s.", location, requestUrl(request)), e);
            }
        }
    }

    @Override
    public <NNREQ, NNRES> HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, HttpResponse, NNREQ, NNRES> chain) {
        HttpResponse response = castToHttpResponse(chain.next(request));
        if (isRedirectResponse(response)) {
            updateHeader(response, "location", request);
        }
        return response;
    }
}
