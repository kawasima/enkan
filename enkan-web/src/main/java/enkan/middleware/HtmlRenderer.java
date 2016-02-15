package enkan.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.collection.Headers;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import static enkan.util.BeanBuilder.builder;

/**
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "htmlRenderer")
public class HtmlRenderer implements Middleware<HttpRequest, HttpResponse> {
    @Override
    public HttpResponse handle(HttpRequest req, MiddlewareChain chain) {
        Object res = chain.next(req);
        HttpResponse response = null;
        if (res instanceof HttpResponse) {
            response = (HttpResponse) res;
        } else if (res instanceof String) {
            response = builder(HttpResponse.of((String) res))
                    .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/plain"))
                    .build();
        }
        return response;
    }
}
