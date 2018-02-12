package enkan.data;

import enkan.collection.Headers;
import enkan.collection.Parameters;

import java.io.InputStream;
import java.util.Map;

/**
 * @author kawasima
 */
public interface HttpRequest
        extends UriAvailable, SessionAvailable, FlashAvailable, PrincipalAvailable, ConversationAvailable, Traceable, Extendable {
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

    Headers getHeaders();

    void setHeaders(Headers headers);

    String getContentType();

    void setContentType(String contentType);

    Long getContentLength();

    void setContentLength(Long contentLength);

    String getCharacterEncoding();

    void setCharacterEncoding(String characterEncoding);

    InputStream getBody();

    void setBody(InputStream body);

    Parameters getParams();

    void setParams(Parameters params);

    Parameters getFormParams();

    void setFormParams(Parameters formParams);

    Parameters getQueryParams();

    void setQueryParams(Parameters queryParams);

    Map<String, Cookie> getCookies();

    void setCookies(Map<String, Cookie> cookies);

    void setExtension(String name, Object extension);

    Object getExtension(String name);
}
