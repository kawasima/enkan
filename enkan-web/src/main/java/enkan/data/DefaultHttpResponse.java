package enkan.data;

import enkan.collection.Headers;
import enkan.collection.Multimap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A default implementation for HTTP response
 *
 * @author kawasima
 */
public class DefaultHttpResponse<T> implements HttpResponse<T> {
    private int status;
    private Headers headers;
    private Multimap<String, Cookie> cookies;
    private Session session;
    private Object body;

    private Map<String, Object> extensions;

    protected DefaultHttpResponse(int status, Headers headers) {
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
    public Headers getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Headers headers) {
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
        return (T) body;
    }

    @Override
    public void setBody(T body) {
        this.body = body;
    }

    @Override
    public String toString() {
        String sb = "{status=" +
                status +
                ", headers=" + Objects.toString(headers.toString(), "{}") +
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
