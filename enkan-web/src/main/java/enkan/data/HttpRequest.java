package enkan.data;

import enkan.collection.Headers;
import enkan.collection.Parameters;

import java.io.InputStream;
import java.util.Map;

/**
 * Represents an incoming HTTP request.
 *
 * <p>This interface aggregates all per-request capabilities through its
 * super-interfaces:
 * <ul>
 *   <li>{@link UriAvailable} — request URI and HTTP method</li>
 *   <li>{@link SessionAvailable} — session access</li>
 *   <li>{@link FlashAvailable} — flash message access</li>
 *   <li>{@link PrincipalAvailable} — authenticated principal</li>
 *   <li>{@link ConversationAvailable} — long-running conversation and its state</li>
 *   <li>{@link Traceable} — distributed trace log</li>
 *   <li>{@link Extendable} — arbitrary named extensions attached by middleware</li>
 * </ul>
 *
 * <p>The default implementation is {@link DefaultHttpRequest}.  Middleware
 * that needs to attach additional capabilities uses
 * {@link enkan.util.MixinUtils#mixin} to return a proxy that also implements
 * the desired extra interfaces (e.g. {@code BodyDeserializable},
 * {@code EntityManageable}).
 *
 * @author kawasima
 */
public interface HttpRequest
        extends UriAvailable, SessionAvailable, FlashAvailable, PrincipalAvailable, ConversationAvailable, Traceable {
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

    <T> void setExtension(String name, T extension);

    <T> T getExtension(String name);
}
