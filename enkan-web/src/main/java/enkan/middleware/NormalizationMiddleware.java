package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import org.eclipse.collections.api.collection.MutableCollection;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.multimap.list.MutableListMultimap;

import java.util.stream.Collectors;

/**
 * The middleware for normalizing parameter values.
 *
 * TODO implements various normalizers and configuration for deciding target parameters.
 *
 * @author kawasima
 */
@Middleware(name = "normalization", dependencies = {"params"})
public class NormalizationMiddleware extends AbstractWebMiddleware {
    public NormalizationMiddleware() {

    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        MutableListMultimap<String, String> params = (MutableListMultimap<String, String>) request.getParams();
        if (params != null) {
            params.forEachKey(key -> {
                params.replaceValues(key, params.get(key)
                        .stream()
                        .map(s -> {
                            return s.trim();
                        })
                        .collect(Collectors.toList()));
            });
        }
        return (HttpResponse) next.next(request);
    }
}
