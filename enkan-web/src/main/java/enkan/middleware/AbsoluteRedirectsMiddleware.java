package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.impl.factory.Sets;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import static enkan.util.HttpRequestUtils.requestUrl;

/**
 * @author kawasima
 */
@Middleware(name = "absoluteRedirect")
public class AbsoluteRedirectsMiddleware extends AbstractWebMiddleware {
    private static final Set<Integer> REDIRECT_STATUS = Sets.immutable.of(201, 301, 302, 303, 307).castToSet();

    protected boolean isRedirectResponse(HttpResponse response) {
        return REDIRECT_STATUS.contains(response.getStatus());
    }

    protected void updateHeader(HttpResponse response, String header, HttpRequest request) {
        RichIterable<String> iterable = response.getHeaders().get(header);
        iterable.each(url -> absoluteUrl(url, request));
    }

    protected boolean isUrl(String s) {
        try {
            new URL(s);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    protected String absoluteUrl(String location, HttpRequest request) {
        if (isUrl(location)) {
            return location;
        } else {
            try {
                URL url = new URL(requestUrl(request));
                return new URL(url, location).toString();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(
                        String.format("wrong location %s or request url %s.", location, requestUrl(request)), e);
            }
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        HttpResponse response = castToHttpResponse(next.next(request));
        if (isRedirectResponse(response)) {
            updateHeader(response, "location", request);
        }
        return response;
    }
}
