package enkan.data;

import org.eclipse.collections.api.multimap.Multimap;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A default implementation for HTTP request
 *
 * @author kawasima
 */
public class DefaultHttpRequest implements HttpRequest {
    private int serverPort;
    private String serverName;
    private String remoteAddr;
    private String uri;
    private String queryString;
    private String scheme;
    private String requestMethod;
    private String protocol;
    private Map<String, String> headers;
    private String contentType;
    private Long contentLength;
    private String characterEncoding;
    private InputStream body;

    private Multimap<String, String> params;
    private Multimap<String, String> formParams;
    private Multimap<String, String> queryParams;

    private Session session;
    private Map<String, Cookie> cookies;
    private Map<String, Object> extensions;

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String getRequestMethod() {
        return requestMethod;
    }

    @Override
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public Long getContentLength() {
        return contentLength;
    }

    @Override
    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    @Override
    public InputStream getBody() {
        return body;
    }

    @Override
    public void setBody(InputStream body) {
        this.body = body;
    }

    @Override
    public Multimap<String, String> getParams() {
        return params;
    }

    @Override
    public void setParams(Multimap<String, String> params) {
        this.params = params;
    }

    @Override
    public Multimap<String, String> getFormParams() {
        return formParams;
    }

    @Override
    public void setFormParams(Multimap<String, String> formParams) {
        this.formParams = formParams;
    }

    @Override
    public Multimap<String, String> getQueryParams() {
        return queryParams;
    }

    @Override
    public void setQueryParams(Multimap<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return cookies;
    }

    @Override
    public void setCookies(Map<String, Cookie> cookies) {
        this.cookies = cookies;
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
    public void setExtension(String name, Object extension) {
        if (extensions == null) {
            extensions = new HashMap<>();
        }
        extensions.put(name, extension);
    }

    @Override
    public Object getExtension(String name) {
        if (extensions == null) {
            extensions = new HashMap<>();
        }
        return extensions.get(name);
    }
}
