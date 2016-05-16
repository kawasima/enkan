package enkan.data;

import enkan.collection.Headers;
import enkan.collection.Multimap;

import java.io.File;
import java.io.InputStream;

/**
 * @author kawasima
 */
public interface HttpResponse<T> extends SessionAvailable, FlashAvailable, Traceable, ConversationAvailable {

    static HttpResponse<String> of(String body) {
        HttpResponse response = new DefaultHttpResponse(200,
                Headers.empty());
        response.setBody(body);
        return response;
    }

    static HttpResponse<InputStream> of(InputStream body) {
        HttpResponse response = new DefaultHttpResponse(200,
                Headers.empty());
        response.setBody(body);
        return response;
    }

    static HttpResponse<File> of(File body) {
        HttpResponse response = new DefaultHttpResponse(200,
                Headers.empty());
        response.setBody(body);
        return response;
    }

    int getStatus();
    void setStatus(int status);

    Headers getHeaders();
    void setHeaders(Headers headers);

    Multimap<String, Cookie> getCookies();
    void setCookies(Multimap<String, Cookie> cookies);

    default void setContentType(String type) {
        getHeaders().put("Content-Type", type);
    }
    T getBody();
    void setBody(T body);

}
