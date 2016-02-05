package enkan.data;

import enkan.collection.Multimap;

import java.io.File;
import java.io.InputStream;

/**
 * @author kawasima
 */
public interface HttpResponse<T> extends SessionAvailable {

    static HttpResponse<String> of(String body) {
        HttpResponse<String> response = new DefaultHttpResponse<>(200,
                Multimap.empty());
        response.setBody(body);
        return response;
    }

    static HttpResponse<InputStream> of(InputStream body) {
        HttpResponse<InputStream> response = new DefaultHttpResponse<>(200,
                Multimap.empty());
        response.setBody(body);
        return response;
    }

    static HttpResponse<File> of(File body) {
        HttpResponse<File> response = new DefaultHttpResponse<>(200,
                Multimap.empty());
        response.setBody(body);
        return response;
    }

    int getStatus();
    void setStatus(int status);

    Multimap<String, Object> getHeaders();
    void setHeaders(Multimap<String, Object> headers);

    Multimap<String, Cookie> getCookies();
    void setCookies(Multimap<String, Cookie> cookies);

    T getBody();
    void setBody(T body);

}
