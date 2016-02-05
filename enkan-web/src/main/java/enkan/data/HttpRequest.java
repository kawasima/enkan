package enkan.data;

import enkan.collection.Multimap;

import java.io.InputStream;
import java.util.Map;

/**
 * @author kawasima
 */
public interface HttpRequest extends UriAvailable, SessionAvailable, Extendable {
    int getServerPort();

    void setServerPort(int serverPort);

    String getServerName();

    void setServerName(String serverName);

    String getRemoteAddr();

    void setRemoteAddr(String remoteAddr);

    String getUri();

    void setUri(String uri);

    String getQueryString();

    void setQueryString(String queryString);

    String getScheme();

    void setScheme(String scheme);

    String getRequestMethod();

    void setRequestMethod(String requestMethod);

    String getProtocol();

    void setProtocol(String protocol);

    Map<String, String> getHeaders();

    void setHeaders(Map<String, String> headers);

    String getContentType();

    void setContentType(String contentType);

    Long getContentLength();

    void setContentLength(Long contentLength);

    String getCharacterEncoding();

    void setCharacterEncoding(String characterEncoding);

    InputStream getBody();

    void setBody(InputStream body);

    Map<String, ?> getParams();

    void setParams(Map<String, ?> params);

    Multimap<String, String> getFormParams();

    void setFormParams(Multimap<String, String> formParams);

    Multimap<String, String> getQueryParams();

    void setQueryParams(Multimap<String, String> queryParams);

    Map<String, Cookie> getCookies();

    void setCookies(Map<String, Cookie> cookies);

    void setExtension(String name, Object extension);

    Object getExtension(String name);
}
