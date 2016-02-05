package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Multimap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.FalteringEnvironmentException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static enkan.util.CodecUtils.formDecode;
import static enkan.util.HttpRequestUtils.characterEncoding;
import static enkan.util.HttpRequestUtils.isUrlEncodedForm;

/**
 * @author kawasima
 */
@Middleware(name = "params")
public class ParamsMiddleware extends AbstractWebMiddleware {
    protected Multimap<String, String> parseParams(String urlencodedParams, String encoding) {
        Multimap<String, String> params = formDecode(urlencodedParams, encoding);
        return params == null ? Multimap.empty() : params;
    }

    protected void parseQueryParams(HttpRequest request, String encoding) {
        String queryString = request.getQueryString();
        if (queryString == null) {
            request.setQueryParams(Multimap.empty());
            if (request.getParams() == null) {
                request.setParams(Multimap.empty());
            }
        } else {
            Multimap<String, String> params = parseParams(queryString, encoding);
            request.setQueryParams(params);
            Multimap<String, Object> current = (Multimap<String, Object>) request.getParams();
            if (current == null) {
                request.setParams(params);
            } else {
                params.keySet().stream()
                        .forEach(k -> {
                            params.getAll(k).forEach(v -> current.add(k, v));
                        });
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
                throw FalteringEnvironmentException.create(e);
            }
            Multimap<String, String> params = parseParams(sb.toString(), encoding);
            request.setFormParams(params);
            Map<String, ?> current = request.getParams();
            if (current == null) {
                request.setParams(params);
            } else {
                // TODO current.putAll(params);
            }
        } else {
            request.setFormParams(Multimap.empty());
            if (request.getParams() == null) {
                request.setParams(Multimap.empty());
            }
        }

    }

    public void paramsRequest(HttpRequest request) {
        String encoding = characterEncoding(request);
        if (encoding == null) {
            encoding = "UTF-8";
        }

        Multimap<String, String> formParams = request.getFormParams();
        if (formParams == null) {
            parseFormParams(request, encoding);
        }

        Multimap<String, String> queryParams = request.getQueryParams();
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
