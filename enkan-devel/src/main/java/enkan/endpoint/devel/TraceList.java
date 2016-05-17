package enkan.endpoint.devel;

import net.unit8.moshas.MoshasEngine;
import net.unit8.moshas.Snippet;
import net.unit8.moshas.Template;

import java.io.Serializable;

import static net.unit8.moshas.RenderUtils.text;

/**
 * @author kawasima
 */
public class TraceList extends MoshasPage implements Serializable {
    private Snippet traceListSnippet;
    private Template traceListPage;

    public TraceList(MoshasEngine moshas) {
        traceListSnippet = moshas.describe("templates/trace/list.html", "#traces > li#trace", t -> {
            t.select("a", (el, ctx) -> el.attr("href", "./" + ctx.getString("log", "id")));
            t.select("a > span.method", text("log", "method"));
            t.select("a > span.uri", text("log", "uri"));
            t.select("span.dateTime", text("log", "dateTime"));
        });

        traceListPage = moshas.describe("templates/trace/list.html", t -> {
            t.select("#primer-link", (el, ctx) -> el.remove());
            t.select("#primer", (el, ctx) -> el.text(primer));
            t.select("#traces", (el, ctx) -> {
                el.empty();
                ctx.getCollection("logs").forEach(
                        stel -> ctx.localScope("log", stel,
                                () -> el.appendChild(traceListSnippet.render(ctx)))
                );
            });
        });
    }

    @Override
    public Template getTemplate() {
        return traceListPage;
    }
}
