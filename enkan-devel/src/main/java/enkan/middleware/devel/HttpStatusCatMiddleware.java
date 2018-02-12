package enkan.middleware.devel;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.AbstractWebMiddleware;
import enkan.util.HttpResponseUtils;
import net.unit8.moshas.MoshasEngine;
import net.unit8.moshas.Template;
import net.unit8.moshas.context.Context;

import java.io.StringWriter;

import static enkan.util.HttpResponseUtils.*;
import static net.unit8.moshas.RenderUtils.*;

/**
 * @author kawasima
 */
@Middleware(name = "httpStatusCat", dependencies = {"contentType"})
public class HttpStatusCatMiddleware extends AbstractWebMiddleware<HttpRequest, HttpResponse> {
    private boolean moreCats;

    private final MoshasEngine moshas = new MoshasEngine();

    private final Template template = moshas.describe("templates/httpStatusCat.html", t -> {
        t.select("head > title", text("status"));
        t.select("img#cat", (el, ctx) -> el.attr("src", "//http.cat/" + ctx.getString("status")));
    });

    public HttpStatusCatMiddleware() {
        moreCats = false;
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, HttpResponse, ? , ?> chain) {
        HttpResponse response = castToHttpResponse(chain.next(request));
        if (response != null && (isEmptyBody(response) || isMoreCats(response))) {
            String type = getHeader(response, "Content-Type");
            if (type == null || "text/html".equals(type)) {
                HttpResponseUtils.header(response, "Content-Type", "text/html");
                Context context = new Context();
                context.setVariable("status", response.getStatus());
                StringWriter sw = new StringWriter();
                template.render(context, sw);

                response.setBody(sw.toString());
            }
        }
        return response;
    }

    private boolean isMoreCats(HttpResponse response) {
        return moreCats && response.getBody() instanceof String
                && response.getBodyAsString().length() < 256;
    }

    public void setMoreCats(boolean moreCats) {
        this.moreCats = moreCats;
    }
}
