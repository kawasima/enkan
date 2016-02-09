package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import enkan.exception.UnreachableException;
import enkan.util.HttpResponseUtils;
import net.unit8.moshas.MoshasEngine;
import net.unit8.moshas.Snippet;
import net.unit8.moshas.Template;
import net.unit8.moshas.context.Context;

import java.io.StringWriter;

import static net.unit8.moshas.RenderUtils.text;

/**
 * @author kawasima
 */
@Middleware(name = "stacktrace")
public class StacktraceMiddleware extends AbstractWebMiddleware {
    private MoshasEngine moshas = new MoshasEngine();

    protected HttpResponse<String> htmlUnreachableExResponse(UnreachableException ex) {
        return HttpResponse.of("This is framework bug. Please report this issue.");
    }

    protected HttpResponse htmlMisconfigExResponse(MisconfigurationException ex) {
        Template template = moshas.defineTemplate("templates/misconfiguration.html", t -> {
            t.select("#misconfiguration", text());
        });
        return HttpResponse.of(ex.getProblem() + "\n" + ex.getSolution());
    }

    protected HttpResponse htmlExResponse(Throwable ex) {
        Snippet stackTraceElementSnippet = moshas.defineSnippet("templates/stacktrace.html", ".trace > table > tbody > tr", s -> {
            s.select("td.source", (el, ctx) ->
                    el.text(ctx.getString("stackTraceElement", "fileName") +
                            ":" +
                            ctx.getString("stackTraceElement", "lineNumber")));
            s.select("td.method", (el, ctx) ->
                    el.text(ctx.getString("stackTraceElement", "className") +
                            "." +
                            ctx.getString("stackTraceElement", "methodName")));
        });

        Template template = moshas.defineTemplate("templates/stacktrace.html", t -> {
            t.select("#class-name", text("exception","message"));
            t.select(".trace table tbody", (el, ctx) -> {
                el.empty();
                ctx.getCollection("exception", "stackTrace").forEach(
                        stel -> ctx.localScope("stackTraceElement", stel,
                                () -> el.appendChild(stackTraceElementSnippet.render(ctx))));
            });
        });

        StringWriter sw = new StringWriter();
        Context ctx = new Context();
        ctx.setVariable("exception", ex);
        template.render(ctx, sw);
        HttpResponse response = HttpResponse.of(sw.toString());
        HttpResponseUtils.contentType(response, "text/html");
        return response;
    }

    protected HttpResponse exResponse(HttpRequest request, Throwable ex) {
        String accept = request.getHeaders().get("accept");
        if (accept != null && accept.matches("^text/javascript")) {
            return null;
        } else {
            if (ex instanceof UnreachableException) {
                return htmlUnreachableExResponse((UnreachableException) ex);
            } else if (ex instanceof MisconfigurationException) {
                return htmlMisconfigExResponse((MisconfigurationException) ex);
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
