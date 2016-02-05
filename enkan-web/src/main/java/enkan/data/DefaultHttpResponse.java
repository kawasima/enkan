package enkan.data;

import enkan.collection.Multimap;

import java.util.HashMap;
import java.util.Map;

/**
 * A default implementation for HTTP response
 *
 * @author kawasima
 */
public class DefaultHttpResponse<T> implements HttpResponse<T> {
    private int status;
    private Multimap<String, Object> headers;
    private Multimap<String, Cookie> cookies;
    private Session session;
    private T body;

    private Map<String, Object> extensions;

    protected DefaultHttpResponse(int status, Multimap<String, Object> headers) {
        this.status = status;
        this.headers = headers;
        this.cookies = Multimap.empty();
        this.extensions = new HashMap<>();
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public Multimap<String, Object> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Multimap<String, Object> headers) {
        this.headers = headers;
    }

    @Override
    public Multimap<String, Cookie> getCookies() {
        return cookies;
    }

    @Override
    public void setCookies(Multimap<String, Cookie> cookies) {
        this.cookies = cookies;
    }

    @Override
    public T getBody() {
        return body;
    }

    @Override
    public void setBody(T body) {
        this.body = body;
    }

    @Override
    public String toString() {
        String sb = "{status=" +
                status +
                ", headers=" + headers.toString() +
                ", body=" + body +
                '}';
        return sb;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public Object getExtension(String name) {
        return extensions.get(name);
    }

    @Override
    public void setExtension(String name, Object extension) {
        extensions.put(name, extension);
    }
}
