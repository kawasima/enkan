package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Multimap;
import enkan.collection.Parameters;
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
        Parameters params = request.getParams();
        if (params != null) {
            params.keySet().stream()
                    .forEach(key -> {
                        Object obj = params.getRawType(key);
                    });
        }
        return (HttpResponse) next.next(request);
    }
}
