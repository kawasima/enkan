package enkan.data;

import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

import java.util.HashMap;
import java.util.Map;

/**
 * A default implementation for HTTP response
 *
 * @author kawasima
 */
public class DefaultHttpResponse<T> implements HttpResponse<T> {
    private int status;
    private MutableMultimap<String, Object> headers;
    private MutableMultimap<String, Cookie> cookies;
    private Session session;
    private T body;

    private Map<String, Object> extensions;

    protected DefaultHttpResponse(int status, MutableMultimap<String, Object> headers) {
        this.status = status;
        this.headers = headers;
        this.cookies = Multimaps.mutable.list.empty();
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
    public MutableMultimap<String, Object> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(MutableMultimap<String, Object> headers) {
        this.headers = headers;
    }

    @Override
    public MutableMultimap<String, Cookie> getCookies() {
        return cookies;
    }

    @Override
    public void setCookies(MutableMultimap<String, Cookie> cookies) {
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
