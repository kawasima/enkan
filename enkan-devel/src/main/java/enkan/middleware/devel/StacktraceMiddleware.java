package enkan.middleware.devel;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Headers;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import enkan.exception.UnreachableException;
import enkan.middleware.AbstractWebMiddleware;
import enkan.util.HttpResponseUtils;
import net.unit8.moshas.MoshasEngine;
import net.unit8.moshas.Snippet;
import net.unit8.moshas.Template;
import net.unit8.moshas.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

import static enkan.util.BeanBuilder.builder;
import static net.unit8.moshas.RenderUtils.text;

/**
 * @author kawasima
 */
@Middleware(name = "stacktrace")
public class StacktraceMiddleware extends AbstractWebMiddleware {
    private MoshasEngine moshas = new MoshasEngine();

    String primer;
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/css/primer.css"), StandardCharsets.ISO_8859_1))) {
            primer = reader.lines().collect(Collectors.joining());
        } catch (Exception e) {
            primer = "";
        }
    }

    Snippet stackTraceElementSnippet = moshas.describe("templates/stacktrace.html", ".trace > table > tbody > tr", s -> {
        s.select("td.source", (el, ctx) ->
                el.text(ctx.getString("stackTraceElement", "fileName") +
                        ":" +
                        ctx.getString("stackTraceElement", "lineNumber")));
        s.select("td.method", (el, ctx) ->
                el.text(ctx.getString("stackTraceElement", "className") +
                        "." +
                        ctx.getString("stackTraceElement", "methodName")));
    });

    protected HttpResponse render(Template template, Object... args) {
        StringWriter sw = new StringWriter();
        Context ctx = new Context();
        for (int i = 0; i < args.length; i += 2) {
            ctx.setVariable(Objects.toString(args[i], ""), args[i+1]);
        }
        template.render(ctx, sw);
        HttpResponse response = HttpResponse.of(sw.toString());
        HttpResponseUtils.contentType(response, "text/html");
        return response;
    }
    protected HttpResponse<String> htmlUnreachableExResponse(UnreachableException ex) {
        Template template = moshas.describe("templates/unreachable.html", t -> {});
        return builder(render(template))
                .set(HttpResponse::setStatus, 500)
                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html; charset=UTF-8"))
                .build();
    }

    protected HttpResponse htmlMisconfigExResponse(MisconfigurationException ex, HttpRequest request) {
        String primer = this.primer;
        Snippet snippet = stackTraceElementSnippet;
        Template template = moshas.describe("templates/misconfiguration.html", t -> {
            t.select("#primer", (el, ctx) -> el.text(primer));
            t.select(".problem", text("exception", "problem"));
            t.select(".solution", text("exception", "solution"));
            t.select(".trace table tbody", (el, ctx) -> {
                el.empty();
                ctx.getCollection("exception", "stackTrace").forEach(
                        stel -> ctx.localScope("stackTraceElement", stel,
                                () -> el.appendChild(snippet.render(ctx))));
            });
            t.select(".request .uri", text("request", "uri"));
            t.select(".request .server-name", text("request", "serverName"));
            t.select(".request .server-port", text("request", "serverPort"));
            t.select(".request .remote-addr", text("request", "remoteAddr"));
            t.select(".request .parameters", text("request", "params"));
        });
        return builder(render(template, "exception", ex, "request", request))
                .set(HttpResponse::setStatus, 500)
                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html; charset=UTF-8"))
                .build();
    }

    protected HttpResponse htmlExResponse(Throwable ex) {
        Snippet snippet = stackTraceElementSnippet;
        Template template = moshas.describe("templates/stacktrace.html", t -> {
            t.select("#class-name", (el, ctx) -> el.text(
                    ctx.get("exception").getClass().getName()));
            t.select(".message", (el, ctx) -> el.text(ctx.getString("exception","message")));
            t.select(".trace table tbody", (el, ctx) -> {
                el.empty();
                ctx.getCollection("exception", "stackTrace").forEach(
                        stel -> ctx.localScope("stackTraceElement", stel,
                                () -> el.appendChild(snippet.render(ctx))));
            });
        });

        return builder(render(template, "exception", ex))
                .set(HttpResponse::setStatus, 500)
                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html; charset=UTF-8"))
                .build();
    }

    protected HttpResponse exResponse(HttpRequest request, Throwable ex) {
        String accept = request.getHeaders().get("accept");
        if (accept != null && accept.matches("^text/javascript")) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            return builder(HttpResponse.of(sw.toString()))
                    .set(HttpResponse::setStatus, 500)
                    .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/javascript"))
                    .build();
        } else {
            if (ex instanceof UnreachableException) {
                return htmlUnreachableExResponse((UnreachableException) ex);
            } else if (ex instanceof MisconfigurationException) {
                return htmlMisconfigExResponse((MisconfigurationException) ex, request);
            } else {
                return htmlExResponse(ex);
            }

        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        try {
            return castToHttpResponse(next.next(request));
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            return exResponse(request, t);
        }
    }
}
