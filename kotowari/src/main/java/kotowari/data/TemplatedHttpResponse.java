package kotowari.data;

import enkan.collection.Multimap;
import enkan.data.DefaultHttpResponse;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kawasima
 */
public class TemplatedHttpResponse extends DefaultHttpResponse<InputStream> {
    private String templateName;
    private Map<String, Object> context;

    private TemplatedHttpResponse(String templateName) {
        super(200, Multimap.empty());
        this.templateName = templateName;
        context = new HashMap<>();
    }

    public static TemplatedHttpResponse create(String templateName, Object... keyVals) {
        TemplatedHttpResponse response = new TemplatedHttpResponse(templateName);
        for (int i = 0; i < keyVals.length; i += 2) {
            response.context.put(keyVals[i].toString(), keyVals[i+1]);
        }
        return response;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
