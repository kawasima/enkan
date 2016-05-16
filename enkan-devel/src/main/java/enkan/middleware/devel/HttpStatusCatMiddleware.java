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
import java.util.List;

import static enkan.util.HttpResponseUtils.getHeader;
import static enkan.util.HttpResponseUtils.isEmptyBody;
import static net.unit8.moshas.RenderUtils.text;

/**
 * @author kawasima
 */
@Middleware(name = "httpStatusCat", dependencies = {"contentType"})
public class HttpStatusCatMiddleware extends AbstractWebMiddleware {
    private MoshasEngine moshas = new MoshasEngine();
    private Template template = moshas.describe("templates/httpStatusCat.html", t -> {
        t.select("head > title", text("status"));
        t.select("img#cat", (el, ctx) -> el.attr("src", "//http.cat/" + ctx.getString("status")));
    });

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        HttpResponse response = castToHttpResponse(next.next(request));
        if (response != null && isEmptyBody(response)) {
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
}
