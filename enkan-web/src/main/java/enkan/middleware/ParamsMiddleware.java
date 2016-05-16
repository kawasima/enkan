package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Parameters;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.FalteringEnvironmentException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static enkan.util.CodecUtils.formDecode;
import static enkan.util.HttpRequestUtils.characterEncoding;
import static enkan.util.HttpRequestUtils.isUrlEncodedForm;

/**
 * @author kawasima
 */
@Middleware(name = "params")
public class ParamsMiddleware extends AbstractWebMiddleware {
    protected Parameters parseParams(String urlencodedParams, String encoding) {
        Parameters params = formDecode(urlencodedParams, encoding);
        return params == null ? Parameters.empty() : params;
    }

    protected void parseQueryParams(HttpRequest request, String encoding) {
        String queryString = request.getQueryString();
        if (queryString == null) {
            request.setQueryParams(Parameters.empty());
            if (request.getParams() == null) {
                request.setParams(Parameters.empty());
            }
        } else {
            Parameters params = parseParams(queryString, encoding);
            request.setQueryParams(params);
            Parameters current = request.getParams();
            if (current == null) {
                request.setParams(params);
            } else {
                current.putAll(params);
            }
        }
    }

    protected void parseFormParams(HttpRequest request, String encoding) {
        InputStream body = request.getBody();
        if (isUrlEncodedForm(request) && body != null) {
            StringBuilder sb = new StringBuilder();
            try (InputStreamReader reader = new InputStreamReader(body, encoding)) {
                for(;;) {
                    int c = reader.read();
                    if (c < 0) break;
                    sb.append((char) c);
                }
            } catch (IOException e) {
                throw new FalteringEnvironmentException(e);
            }
            Parameters params = parseParams(sb.toString(), encoding);
            request.setFormParams(params);
            Parameters current = request.getParams();
            if (current == null) {
                request.setParams(params);
            } else {
                current.putAll(params);
            }
        } else {
            request.setFormParams(Parameters.empty());
            if (request.getParams() == null) {
                request.setParams(Parameters.empty());
            }
        }

    }

    public void paramsRequest(HttpRequest request) {
        String encoding = characterEncoding(request);
        if (encoding == null) {
            encoding = "UTF-8";
        }

        Parameters formParams = request.getFormParams();
        if (formParams == null) {
            parseFormParams(request, encoding);
        }

        Parameters queryParams = request.getQueryParams();
        if (queryParams == null) {
            parseQueryParams(request, encoding);
        }
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest, MiddlewareChain next) {
        paramsRequest(httpRequest);
        return (HttpResponse) next.next(httpRequest);
    }
}
