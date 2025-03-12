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
 * Middleware for handling multipart form data.
 * It parses the body of the request and puts the parameters into the request object.
 * It also deletes temporary files after processing the request.
 * The temporary files are stored in the request object as well.
 * If you want to use this middleware, you must add the {@link enkan.middleware.ParamsMiddleware} before it.
 * <p>
 * This middleware is not thread-safe.
 *
 * @param <NRES> the type of the response object
 * @author kawasima
 */
@Middleware(name = "multipartParams", dependencies = {"params"})
public class MultipartParamsMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    /**
     * Deletes temporary files.
     *
     * @param multipartParams the parameters extracted from the multipart form data
     */
    protected void deleteTempFile(Parameters multipartParams) {
        multipartParams.keySet().stream()
                .filter(k -> {
                    Object v = multipartParams.getIn(k);
                    return v instanceof Parameters && ((Parameters) v).getIn("tempfile") instanceof File;
                })
                .forEach(k -> {
                    Optional<Path> tempFile = ThreadingUtils.some((File) multipartParams.getIn(k, "tempfile"),
                            File::toPath);
                    tempFile.ifPresent(f -> {
                        try {
                            Files.deleteIfExists(f);
                        } catch (IOException ex) {
                            throw new FalteringEnvironmentException(ex);
                        }
                    });
                });
    }

    /**
     * Extracts multipart form data from the request.
     *
     * @param request the request object
     * @return the parameters extracted from the multipart form data
     */
    protected Parameters extractMultipart(HttpRequest request) {
        try {
            return MultipartParser.parse(request.getBody(), contentLength(request),
                    request.getHeaders().get("content-type"), 16384);
        } catch (IOException e) {
            throw new FalteringEnvironmentException(e);
        }
    }

    /**
     * Handles the request.
     *
     * @param request   A request object
     * @param chain A chain of middlewares
     * @return A response object
     * @param <NNREQ> the type of the next request object
     * @param <NNRES> the type of the next response object
     */
    @Override
    public <NNREQ, NNRES> HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, NNREQ, NNRES> chain) {
        Parameters multipartParams = extractMultipart(request);
        request.getParams().putAll(multipartParams);
        try {
            return castToHttpResponse(chain.next(request));
        } finally {
            deleteTempFile(multipartParams);
        }

    }
}
