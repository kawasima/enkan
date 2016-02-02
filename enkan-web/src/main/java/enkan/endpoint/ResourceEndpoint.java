package enkan.endpoint;

import enkan.Endpoint;
import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.util.HttpResponseUtils;

/**
 * @author kawasima
 */
public class ResourceEndpoint implements Endpoint<HttpRequest, HttpResponse> {
    private String path;

    public ResourceEndpoint(String path) {
        this.path = path;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        return HttpResponseUtils.resourceResponse(path, OptionMap.empty());
    }
}
