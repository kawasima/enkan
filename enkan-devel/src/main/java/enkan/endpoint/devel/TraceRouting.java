package enkan.endpoint.devel;

import enkan.collection.Headers;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.PathPredicate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static enkan.util.BeanBuilder.builder;

/**
 * @author kawasima
 */
public class TraceRouting {
    private String baseUri;
    private List<Route> routes = new ArrayList<>();

    public TraceRouting(String baseUri) {
        this.baseUri = baseUri;
    }

    public void add(String path, BiConsumer<HttpRequest, OutputStream> action) {
        routes.add(new Route(PathPredicate.GET(baseUri + path), action));
    }

    public HttpResponse handle(HttpRequest request) {
        Optional<Route> found = routes.stream()
                .filter(route -> route.predicate.test(request))
                .findFirst();
        if (found.isPresent()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            found.get().action.accept(request, baos);
            return builder(HttpResponse.of(new ByteArrayInputStream(baos.toByteArray())))
                    .set(HttpResponse::setHeaders,
                            Headers.of("Content-Type", "text/html"))
                    .build();
        } else {
            return builder(HttpResponse.of("Not Found"))
                    .set(HttpResponse::setHeaders,
                            Headers.of("Content-Type", "text/html"))
                    .set(HttpResponse::setStatus, 404)
                    .build();
        }
    }

    private static class Route {
        PathPredicate<HttpRequest> predicate;
        BiConsumer<HttpRequest, OutputStream> action;

        public Route(PathPredicate<HttpRequest> predicate, BiConsumer<HttpRequest, OutputStream> action) {
            this.predicate = predicate;
            this.action = action;
        }
    }
}
