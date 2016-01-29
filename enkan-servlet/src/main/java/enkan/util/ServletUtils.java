package enkan.util;

import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.FalteringEnvironmentException;
import enkan.exception.UnreachableException;
import enkan.exception.UnrecoverableException;
import org.eclipse.collections.api.multimap.Multimap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * @author kawasima
 */
public class ServletUtils {
    private static Map<String, String> getHeaders(HttpServletRequest servletRequest) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = servletRequest.getHeaderNames();
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            Enumeration<String> valueEnumeration = servletRequest.getHeaders(name);
            List<String> values = new ArrayList<>();
            while(valueEnumeration.hasMoreElements()) {
                values.add(valueEnumeration.nextElement());
            }
            headers.put(name.toLowerCase(Locale.ENGLISH), String.join(",", values));
        }
        return headers;
    }

    private static Long getContentLength(HttpServletRequest servletRequest) {
        long length = servletRequest.getContentLengthLong();
        return length >= 0 ? length : null;
    }

    public static HttpRequest buildRequest(HttpServletRequest servletRequest) throws IOException {
        HttpRequest request = new DefaultHttpRequest();
        request.setServerPort(servletRequest.getServerPort());
        request.setServerName(servletRequest.getServerName());
        request.setRemoteAddr(servletRequest.getRemoteAddr());
        request.setUri(servletRequest.getRequestURI());
        request.setQueryString(servletRequest.getQueryString());
        request.setHeaders(getHeaders(servletRequest));
        request.setScheme(servletRequest.getScheme());
        request.setRequestMethod(servletRequest.getMethod().toLowerCase(Locale.ENGLISH));
        request.setProtocol(servletRequest.getProtocol());
        request.setContentType(servletRequest.getContentType());
        request.setContentLength(getContentLength(servletRequest));
        request.setCharacterEncoding(servletRequest.getCharacterEncoding());
        request.setBody(servletRequest.getInputStream());
        return request;
    }

    private static void setHeaders(HttpServletResponse servletResponse, Multimap<String, Object> headers) {
        headers.forEachKeyValue((k, v) -> {
            if (v == null) return;
            if (servletResponse.getHeaders(k).isEmpty()) {
                servletResponse.setHeader(k, v.toString());
            } else {
                servletResponse.addHeader(k, v.toString());
            }
        });
    }

    private static void setBody(HttpServletResponse servletResponse, Object body) throws IOException {
        if (body == null) {
            return; // Do nothing
        }

        if (body instanceof String) {
            try(PrintWriter writer = servletResponse.getWriter()) {
                writer.print((String) body);
            }
        } else if (body instanceof InputStream) {
            InputStream input = (InputStream) body;
            try (ServletOutputStream output = servletResponse.getOutputStream()) {
                byte[] buf = new byte[4096];
                for (; ; ) {
                    int size = input.read(buf);
                    if (size <= 0) break;
                    output.write(buf, 0, size);
                }
            }
        } else if (body instanceof File) {
            try(InputStream in = new FileInputStream((File) body)) {
                setBody(servletResponse, in);
            }
        } else {
            throw UnreachableException.create();
        }
    }

    public static void updateServletResponse(HttpServletResponse servletResponse, HttpResponse response) {
        if (servletResponse == null || response == null) return;

        servletResponse.setStatus(response.getStatus());
        setHeaders(servletResponse, response.getHeaders());
        try {
            setBody(servletResponse, response.getBody());
        } catch(IOException ex) {
            FalteringEnvironmentException.create(ex);
        }
    }
}
