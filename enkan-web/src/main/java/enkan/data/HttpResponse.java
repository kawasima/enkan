package enkan.data;

import enkan.collection.Headers;
import enkan.collection.Multimap;

import java.io.File;
import java.io.InputStream;

/**
 * Represents an HTTP response.
 *
 * @author kawasima
 */
public interface HttpResponse extends SessionAvailable, FlashAvailable, Traceable, ConversationAvailable {
    /**
     * Creates HttpResponse with the given String body.
     *
     * @param body String body
     * @return the response object
     */
    static HttpResponse of(String body) {
        HttpResponse response = new DefaultHttpResponse(200,
                Headers.empty());
        response.setBody(body);
        return response;
    }

    /**
     * Creates HttpResponse with the given Stream body.
     *
     * @param body Stream body
     * @return the response object
     */
    static HttpResponse of(InputStream body) {
        HttpResponse response = new DefaultHttpResponse(200,
                Headers.empty());
        response.setBody(body);
        return response;
    }

    /**
     * Creates HttpResponse with the given file body.
     *
     * @param body file body
     * @return the response object
     */
    static HttpResponse of(File body) {
        HttpResponse response = new DefaultHttpResponse(200,
                Headers.empty());
        response.setBody(body);
        return response;
    }

    /**
     * Returns a HTTP status code.
     *
     * @return the current status code of this response
     */
    int getStatus();

    /**
     * Sets a HTTP status code.
     *
     * @param status a status code.
     */
    void setStatus(int status);

    Headers getHeaders();
    void setHeaders(Headers headers);

    Multimap<String, Cookie> getCookies();
    void setCookies(Multimap<String, Cookie> cookies);

    default void setContentType(String type) {
        getHeaders().put("Content-Type", type);
    }

    /**
     * Returns body as String.
     *
     * @return the body of this response
     */
    String getBodyAsString();

    /**
     * Returns body as InputStream.
     *
     * @return the body of this response
     */
    InputStream getBodyAsStream();

    /**
     * Returns raw body.
     *
     * @return the raw body of this response
     */
    Object getBody();

    void setBody(String body);
    void setBody(InputStream body);
    void setBody(File body);

}
