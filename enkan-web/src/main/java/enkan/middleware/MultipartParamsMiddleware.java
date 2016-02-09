package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.FalteringEnvironmentException;
import enkan.middleware.multipart.MultipartParser;

import java.io.IOException;

import static enkan.util.HttpRequestUtils.contentLength;
import static enkan.util.HttpRequestUtils.contentType;

/**
 * @author kawasima
 */
@Middleware(name = "multipartParams")
public class MultipartParamsMiddleware extends AbstractWebMiddleware {
    protected void extractMultipart(HttpRequest request) {
        try {
            MultipartParser.parse(request.getBody(), contentLength(request), contentType(request), 16384);
        } catch (IOException e) {
            throw FalteringEnvironmentException.create(e);
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
        extractMultipart(request);
        return castToHttpResponse(chain.next(request));
    }
}
