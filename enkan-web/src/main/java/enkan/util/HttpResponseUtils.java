package enkan.util;

import enkan.collection.Headers;
import enkan.collection.OptionMap;
import enkan.data.HttpResponse;
import enkan.exception.FalteringEnvironmentException;
import enkan.exception.MisconfigurationException;
import enkan.exception.UnreachableException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static enkan.util.BeanBuilder.builder;

/**
 * Utilities for HTTP response.
 *
 * @author kawasima
 */
public class HttpResponseUtils {
    public enum RedirectStatusCode {
        MOVED_PERMANENTLY(301),
        FOUND(302),
        SEE_OTHER(303),
        TEMPORARY_REDIRECT(307),
        PERMANENT_REDIRECT(308);

        private int code;

        RedirectStatusCode(int statusCode) {
            this.code = statusCode;
        }

        public int getStatusCode() { return code; }
    }


    /**
     * Create a response with a Location header.
     *
     * @param url   a redirect url
     * @param code  a HTTP status code in redirect
     * @return response
     */
    public static HttpResponse redirect(String url, RedirectStatusCode code) {
        return builder(HttpResponse.of(""))
                .set(HttpResponse::setStatus, code.getStatusCode())
                .set(HttpResponse::setHeaders, Headers.of("Location", url))
                .build();
    }

    /**
     * Create a response with a string body.
     *
     * @param body string body
     * @return response
     */
    public static HttpResponse response(String body) {
        return builder(HttpResponse.of(body))
                .set(HttpResponse::setStatus, 200)
                .build();
    }


    /**
     * Returns the header value of the given response.
     *
     * @param response a response object
     * @param name the name of the header
     * @param <T>  the type of the header value
     * @return the header value
     */
    public static <T> T getHeader(HttpResponse response, String name) {
        return (T) response.getHeaders().get(name);
    }

    /**
     * Sets a response header with the given name and value
     *
     * @param response a response object
     * @param name the name of the header
     * @param value the header value
     */
    public static void header(HttpResponse response, String name, String value) {
        response.getHeaders().put(name, value);
    }

    /**
     * Sets a charset to the response.
     *
     * @param response a response object
     * @param charset the name of the character set
     */
    public static void charset(HttpResponse response, String charset) {
        String type = getHeader(response, "Content-Type");
        if (type == null) {
            type = "text/plain";
        }
        String newType = type.replaceAll(";\\s*charset=[^;]*", "") + "; charset=" + charset;
        response.getHeaders().remove("Content-Type");
        header(response, "Content-Type", newType);
    }

    /**
     * Sets a content type to the response.
     *
     * @param response a response object
     * @param type the type of the content
     * @return a HttpResponse contains content-type header
     */
    public static HttpResponse contentType(HttpResponse response, String type) {
        if (type != null) {
            response.getHeaders().remove("Content-Type");
            response.getHeaders().put("Content-Type", type);
        }
        return response;
    }

    /**
     * Sets a size of the response message.
     *
     * @param response a response object
     * @param len the length of response message
     * @return a HttpResponse contains content-length header
     */
    public static HttpResponse contentLength(HttpResponse response, Long len) {
        if (len != null) {
            response.getHeaders().remove("Content-Length");
            response.getHeaders().put("Content-Length", len);
        }
        return response;
    }

    public static HttpResponse lastModified(HttpResponse response, Date lastModified) {
        if (lastModified != null) {
            response.getHeaders().remove("Last-Modified");
            response.getHeaders().put("Last-Modified", HttpDateFormat.RFC1123.format(lastModified));
        }
        return response;
    }

    private static String addEndingSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    public static boolean isJarDirectory(JarURLConnection conn) throws IOException {
        JarFile jarFile = conn.getJarFile();
        String entryName = conn.getEntryName();
        ZipEntry dirEntry = jarFile.getEntry(addEndingSlash(entryName));
        return dirEntry != null && dirEntry.isDirectory();
    }

    public static Long connectionContentLength(URLConnection conn) {
        long len = conn.getContentLengthLong();
        return len <= 0 ? null : len;
    }

    public static Date connectionLastModified(URLConnection conn) {
        long lastMod = conn.getLastModified();
        return lastMod > 0 ? new Date(lastMod) : null;
    }

    public static ContentData resourceData(URL url) {
        String protocol = url.getProtocol();
        if ("file".equals(protocol)) {
            try {
                File file = new File(url.toURI());
                if (!file.isDirectory()) {
                    return new FileContentData(file, file.length(),
                            new Date((file.lastModified() / 1000) * 1000));
                }
            } catch (URISyntaxException e) {
                throw new UnreachableException(e);
            }
        } else if ("jar".equals(protocol)) {
            try {
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                if (connection != null && isJarDirectory(connection)) {
                    return new StreamContentData(connection.getInputStream(),
                            connectionContentLength(connection),
                            connectionLastModified(connection));
                }
            } catch (IOException e) {
                throw new FalteringEnvironmentException(e);
            }
        }

        return null;
    }

    public static HttpResponse urlResponse(URL url) {
        ContentData data = resourceData(url);
        if (data == null) return null;

        HttpResponse response;
        if (data instanceof FileContentData) {
            response = HttpResponse.of(((FileContentData) data).getContent());
        } else if (data instanceof StreamContentData) {
            response = HttpResponse.of(((StreamContentData) data).getContent());
        } else {
            throw new MisconfigurationException("web.CLASSPATH", url.getProtocol(), url);
        }
        contentLength(response, data.getContentLength());
        lastModified(response, data.getLastModifiedDate());
        return response;
    }

    public static HttpResponse resourceResponse(String path, OptionMap options) {
        String root = options.getString("root");
        path = (root != null ? root : "") + "/" + path;
        path = path.replace("//", "/").replaceAll("^/", "");

        ClassLoader loader = (ClassLoader) options.get("loader");
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        URL url = loader.getResource(path);
        return url != null ? urlResponse(url) : null;
    }

    public static boolean isEmptyBody(HttpResponse response) {
        Object body = response.getBody();
        return (body == null || (body instanceof String && ((String) body).isEmpty()));
    }

    private static abstract class ContentData<T> implements Serializable {
        private T content;
        private Long contentLength;
        private Date lastModifiedDate;

        public ContentData(T content, Long contentLength, Date lastModifiedDate) {
            this.content = content;
            this.contentLength = contentLength;
            this.lastModifiedDate = lastModifiedDate;
        }

        public T getContent() {
            return content;
        }

        public void setContent(T content) {
            this.content = content;
        }

        public Long getContentLength() {
            return contentLength;
        }

        public void setContentLength(Long contentLength) {
            this.contentLength = contentLength;
        }

        public Date getLastModifiedDate() {
            return lastModifiedDate;
        }

        public void setLastModifiedDate(Date lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
        }
    }

    private static class FileContentData extends ContentData<File> {
        public FileContentData(File content, Long contentLength, Date lastModifiedDate) {
            super(content, contentLength, lastModifiedDate);
        }
    }

    private static class StreamContentData extends ContentData<InputStream> {
        public StreamContentData(InputStream content, Long contentLength, Date lastModifiedDate) {
            super(content, contentLength, lastModifiedDate);
        }
    }

}
