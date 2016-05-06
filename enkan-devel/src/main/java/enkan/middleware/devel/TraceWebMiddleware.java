package enkan.middleware.devel;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Headers;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Traceable;
import enkan.middleware.AbstractWebMiddleware;
import enkan.middleware.session.KeyValueStore;
import enkan.middleware.session.MemoryStore;
import enkan.util.MixinUtils;
import net.unit8.moshas.MoshasEngine;
import net.unit8.moshas.RenderUtils;
import net.unit8.moshas.Snippet;
import net.unit8.moshas.Template;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static enkan.util.BeanBuilder.builder;
import static net.unit8.moshas.RenderUtils.*;

/**
 * @author kawasima
 */
@Middleware(name = "traceWeb")
public class TraceWebMiddleware extends AbstractWebMiddleware {
    private MoshasEngine moshas = new MoshasEngine();
    private Snippet traceListSnippet = moshas.defineSnippet("templates/trace/list.html", "#traces > li#trace", t -> {
        t.select("a", attr("href", "id"));
        t.select("a > span.method", text("method"));
    });

    private Template traceListPage = moshas.defineTemplate("templates/httpStatusCat.html", t -> {
        t.select("#traces", (el, ctx) -> {
            el.empty();
            ctx.getCollection("traces").forEach(
                    stel -> ctx.localScope("log", stel,
                            () -> el.appendChild(traceListSnippet.render(ctx)))
            );
        });
    });

    private List<LogKey> idList;
    private KeyValueStore store;
    private String mountPath = "/x-enkan/requests";

    public TraceWebMiddleware() {
        store = new MemoryStore();
        idList = new ArrayList<>();
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        if (request.getUri().startsWith(mountPath + "/")) {
            String lines = idList.stream().map(k -> k.getUri() + ":" + k.getDateTime())
                    .collect(Collectors.joining("\n"));
            HttpResponse response = builder(HttpResponse.of(lines))
                    .set(HttpResponse::setHeaders, Headers.of("content-type", "text/plain"))
                    .build();
            return response;
        } else {
            request = MixinUtils.mixin(request, Traceable.class);
            HttpResponse response = castToHttpResponse(next.next(request));
            Traceable traceable = Traceable.class.cast(request);
            store.write(traceable.getId(), traceable.getTraceLog());
            idList.add(new LogKey(traceable.getId(), request.getUri()));

            return response;
        }
    }

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    private static class LogKey implements Serializable, Comparable<LogKey> {
        private String id;
        private String uri;
        private LocalDateTime dateTime;

        public LogKey(String id, String uri) {
            this.id = id;
            this.uri = uri;
            this.dateTime = LocalDateTime.now();
        }

        public String getId() {
            return id;
        }

        public String getUri() {
            return uri;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object another) {
            if (another != null && LogKey.class.isInstance(another)) {
                return Objects.equals(this.id, ((LogKey) another).getId());
            }
            return false;
        }

        @Override
        public int compareTo(LogKey another) {
            return this.dateTime.compareTo(another.getDateTime());
        }
    }
}
