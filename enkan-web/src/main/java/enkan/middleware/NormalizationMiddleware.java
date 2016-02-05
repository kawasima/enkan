package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Multimap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import java.util.Map;

/**
 * The middleware for normalizing parameter values.
 *
 * TODO implements various normalizers and configuration for deciding target parameters.
 *
 * @author kawasima
 */
@Middleware(name = "normalization", dependencies = {"params"})
public class NormalizationMiddleware extends AbstractWebMiddleware {
    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        Map<String, ?> params = request.getParams();
        if (params != null) {
            params.keySet().stream()
                    .forEach(key -> {
                        if (params instanceof Multimap) {
                            Multimap<String, String> mm = Multimap.class.cast(params);
                            mm.replaceEachValues(key, val -> val.trim());
                        }
                    });
        }
        return (HttpResponse) next.next(request);
    }
}
