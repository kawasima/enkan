package enkan.data;

import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

import java.io.File;
import java.io.InputStream;

/**
 * @author kawasima
 */
public interface HttpResponse<T> extends SessionAvailable {

    static HttpResponse<String> of(String body) {
        HttpResponse<String> response = new DefaultHttpResponse<>(200,
                Multimaps.mutable.list.with("content-type", "text/html"));
        response.setBody(body);
        return response;
    }

    static HttpResponse<InputStream> of(InputStream body) {
        HttpResponse<InputStream> response = new DefaultHttpResponse<>(200,
                Multimaps.mutable.list.with("content-type", "text/html"));
        response.setBody(body);
        return response;
    }

    static HttpResponse<File> of(File body) {
        HttpResponse<File> response = new DefaultHttpResponse<>(200,
                Multimaps.mutable.list.with("content-type", "text/html"));
        response.setBody(body);
        return response;
    }

    int getStatus();
    void setStatus(int status);

    MutableMultimap<String, Object> getHeaders();
    void setHeaders(MutableMultimap<String, Object> headers);

    MutableMultimap<String, Cookie> getCookies();
    void setCookies(MutableMultimap<String, Cookie> cookies);

    T getBody();
    void setBody(T body);

}
