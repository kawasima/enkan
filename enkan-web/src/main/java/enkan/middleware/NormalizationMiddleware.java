package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Parameters;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.normalizer.Normalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * The middleware for normalizing parameter values.
 *
 * @author kawasima
 */
@Middleware(name = "normalization", dependencies = {"params"})
public class NormalizationMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    private List<NormalizationSpec<Object>> normalizationSpecs;

    public NormalizationMiddleware() {
        this.normalizationSpecs = new ArrayList<>();
    }

    public NormalizationMiddleware(NormalizationSpec<Object> spec, NormalizationSpec<Object>... specs) {
        this();
        normalizationSpecs.add(spec);
        normalizationSpecs.addAll(Arrays.asList(specs));
    }

    public static <T> NormalizationSpec<T> normalization(Predicate<String> predicate, Normalizer<T> normalizer) {
        return new NormalizationSpec<>(predicate, normalizer);
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> chain) {
        Parameters params = request.getParams();
        if (params != null) {
            params.keySet().forEach(key -> {
                Object obj = params.getRawType(key);
                if (obj == null) return;

                normalizationSpecs.forEach(c -> {
                    if (c.getPredicate().test(key) && c.getNormalizer().canNormalize(obj.getClass())) {
                        params.replace(key, c.getNormalizer().normalize(obj));
                    }
                });
            });
        }
        return castToHttpResponse(chain.next(request));
    }

    public static class NormalizationSpec<T> {
        private Normalizer<T> normalizer;
        private Predicate<String> predicate;

        public NormalizationSpec(Predicate<String> predicate, Normalizer<T> normalizer) {
            this.predicate = predicate;
            this.normalizer = normalizer;
        }

        public Normalizer<T> getNormalizer() {
            return normalizer;
        }

        public Predicate<String> getPredicate() {
            return predicate;
        }
    }
}
