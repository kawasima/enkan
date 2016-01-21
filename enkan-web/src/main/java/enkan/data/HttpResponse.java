package enkan.data;

import org.eclipse.collections.api.multimap.Multimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kawasima
 */
public class HttpResponse<T> {
    private int status;
    private MutableMultimap<String, Object> headers;
    private T body;

    protected HttpResponse(int status, MutableMultimap<String, Object> headers) {
        this.status = status;
        this.headers = headers;
    }
    public static HttpResponse<String> of(String body) {
        HttpResponse<String> response = new HttpResponse<>(200,
                Multimaps.mutable.list.with("content-type", "text/html"));
        response.setBody(body);
        return response;
    }

    public static HttpResponse<InputStream> of(InputStream body) {
        HttpResponse<InputStream> response = new HttpResponse<>(200,
                Multimaps.mutable.list.with("content-type", "text/html"));
        response.setBody(body);
        return response;
    }

    public static HttpResponse<File> of(File body) {
        HttpResponse<File> response = new HttpResponse<>(200,
                Multimaps.mutable.list.with("content-type", "text/html"));
        response.setBody(body);
        return response;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MutableMultimap<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(MutableMultimap<String, Object> headers) {
        this.headers = headers;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append("{status=")
                .append(status)
                .append(", headers=").append(headers.toString())
                .append(", body=").append(body)
                .append('}').toString();
    }
}
