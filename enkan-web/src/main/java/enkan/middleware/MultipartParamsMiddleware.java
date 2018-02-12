package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Parameters;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.FalteringEnvironmentException;
import enkan.middleware.multipart.MultipartParser;
import enkan.util.ThreadingUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static enkan.util.HttpRequestUtils.contentLength;

/**
 * @author kawasima
 */
@Middleware(name = "multipartParams", dependencies = {"params"})
public class MultipartParamsMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    protected void deleteTempfile(Parameters multipartParams) {
        multipartParams.keySet().stream()
                .filter(k -> {
                    Object v = multipartParams.getIn(k);
                    return v instanceof Parameters && ((Parameters) v).getIn("tempfile") instanceof File;
                })
                .forEach(k -> {
                    Optional<Path> tempfile = ThreadingUtils.some((File) multipartParams.getIn(k, "tempfile"),
                            File::toPath);
                    tempfile.ifPresent(f -> {
                        try {
                            Files.deleteIfExists(f);
                        } catch (IOException ex) {
                            throw new FalteringEnvironmentException(ex);
                        }
                    });
                });
    }

    protected Parameters extractMultipart(HttpRequest request) {
        try {
            return MultipartParser.parse(request.getBody(), contentLength(request),
                    request.getHeaders().get("content-type"), 16384);
        } catch (IOException e) {
            throw new FalteringEnvironmentException(e);
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> chain) {
        Parameters multipartParams = extractMultipart(request);
        request.getParams().putAll(multipartParams);
        try {
            return castToHttpResponse(chain.next(request));
        } finally {
            deleteTempfile(multipartParams);
        }

    }
}
