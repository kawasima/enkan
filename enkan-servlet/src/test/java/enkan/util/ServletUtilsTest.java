package enkan.util;

import enkan.collection.Headers;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.UnreachableException;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author kawasima
 */
class ServletUtilsTest {

    // ---------------------------------------------------------------- buildRequest

    @Test
    void buildRequestMapsBasicFields() throws IOException {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getServerPort()).thenReturn(8080);
        when(servletRequest.getServerName()).thenReturn("localhost");
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(servletRequest.getRequestURI()).thenReturn("/foo/bar");
        when(servletRequest.getQueryString()).thenReturn("q=1");
        when(servletRequest.getScheme()).thenReturn("http");
        when(servletRequest.getMethod()).thenReturn("GET");
        when(servletRequest.getProtocol()).thenReturn("HTTP/1.1");
        when(servletRequest.getContentType()).thenReturn("application/json");
        when(servletRequest.getContentLengthLong()).thenReturn(42L);
        when(servletRequest.getCharacterEncoding()).thenReturn("UTF-8");
        when(servletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(servletRequest.getInputStream()).thenReturn(mock(ServletInputStream.class));

        HttpRequest request = ServletUtils.buildRequest(servletRequest);

        assertThat(request.getServerPort()).isEqualTo(8080);
        assertThat(request.getServerName()).isEqualTo("localhost");
        assertThat(request.getRemoteAddr()).isEqualTo("127.0.0.1");
        assertThat(request.getUri()).isEqualTo("/foo/bar");
        assertThat(request.getQueryString()).isEqualTo("q=1");
        assertThat(request.getScheme()).isEqualTo("http");
        assertThat(request.getRequestMethod()).isEqualTo("GET");
        assertThat(request.getProtocol()).isEqualTo("HTTP/1.1");
        assertThat(request.getContentLength()).isEqualTo(42L);
        assertThat(request.getCharacterEncoding()).isEqualTo("UTF-8");
    }

    @Test
    void buildRequestPreservesHttpMethodCase() throws IOException {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn("POST");
        when(servletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(servletRequest.getInputStream()).thenReturn(mock(ServletInputStream.class));
        when(servletRequest.getContentLengthLong()).thenReturn(-1L);

        HttpRequest request = ServletUtils.buildRequest(servletRequest);

        assertThat(request.getRequestMethod()).isEqualTo("POST");
    }

    @Test
    void buildRequestMapsHeaders() throws IOException {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn("GET");
        when(servletRequest.getContentLengthLong()).thenReturn(-1L);
        when(servletRequest.getInputStream()).thenReturn(mock(ServletInputStream.class));
        when(servletRequest.getHeaderNames())
                .thenReturn(Collections.enumeration(List.of("Content-Type", "X-Custom")));
        when(servletRequest.getHeaders("Content-Type"))
                .thenReturn(Collections.enumeration(List.of("application/json")));
        when(servletRequest.getHeaders("X-Custom"))
                .thenReturn(Collections.enumeration(List.of("value1")));

        HttpRequest request = ServletUtils.buildRequest(servletRequest);

        assertThat(request.getHeaders().getRawType("Content-Type")).isEqualTo("application/json");
        assertThat(request.getHeaders().getRawType("X-Custom")).isEqualTo("value1");
    }

    @Test
    void buildRequestStoresEachMultiValueHeaderSeparately() throws IOException {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn("GET");
        when(servletRequest.getContentLengthLong()).thenReturn(-1L);
        when(servletRequest.getInputStream()).thenReturn(mock(ServletInputStream.class));
        when(servletRequest.getHeaderNames())
                .thenReturn(Collections.enumeration(List.of("Set-Cookie")));
        when(servletRequest.getHeaders("Set-Cookie"))
                .thenReturn(Collections.enumeration(List.of("a=1", "b=2")));

        HttpRequest request = ServletUtils.buildRequest(servletRequest);

        // 複数値のヘッダーはカンマ結合されず個別に格納される
        assertThat(request.getHeaders().getList("Set-Cookie")).containsExactly("a=1", "b=2");
    }

    @Test
    void buildRequestReturnsNullContentLengthWhenNegative() throws IOException {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn("GET");
        when(servletRequest.getContentLengthLong()).thenReturn(-1L);
        when(servletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(servletRequest.getInputStream()).thenReturn(mock(ServletInputStream.class));

        HttpRequest request = ServletUtils.buildRequest(servletRequest);

        assertThat(request.getContentLength()).isNull();
    }

    // --------------------------------------------------------- updateServletResponse

    @Test
    void updateServletResponseSetsStatusCode() throws IOException {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        when(servletResponse.getHeaders(any())).thenReturn(Collections.emptyList());
        when(servletResponse.getWriter()).thenReturn(mock(PrintWriter.class));

        HttpResponse response = HttpResponse.of("ok");
        response.setStatus(201);
        response.setHeaders(Headers.empty());

        ServletUtils.updateServletResponse(servletResponse, response);

        verify(servletResponse).setStatus(201);
    }

    @Test
    void updateServletResponseSetsStringBody() throws IOException {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        when(servletResponse.getHeaders(any())).thenReturn(Collections.emptyList());
        PrintWriter writer = mock(PrintWriter.class);
        when(servletResponse.getWriter()).thenReturn(writer);

        HttpResponse response = builder(HttpResponse.of("hello"))
                .set(HttpResponse::setHeaders, Headers.empty())
                .build();

        ServletUtils.updateServletResponse(servletResponse, response);

        verify(writer).print("hello");
    }

    @Test
    void updateServletResponseSetsInputStreamBody() throws IOException {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        when(servletResponse.getHeaders(any())).thenReturn(Collections.emptyList());
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(jakarta.servlet.WriteListener l) {}
            @Override public void write(int b) { captured.write(b); }
            @Override public void write(byte[] b, int off, int len) { captured.write(b, off, len); }
        };
        when(servletResponse.getOutputStream()).thenReturn(sos);

        byte[] bytes = "stream".getBytes(StandardCharsets.UTF_8);
        HttpResponse response = builder(HttpResponse.of(new ByteArrayInputStream(bytes)))
                .set(HttpResponse::setHeaders, Headers.empty())
                .build();

        ServletUtils.updateServletResponse(servletResponse, response);

        assertThat(captured.toString(StandardCharsets.UTF_8)).isEqualTo("stream");
    }

    @Test
    void updateServletResponseSetsFileBody() throws IOException {
        File tmp = File.createTempFile("servlet-test-", ".txt");
        tmp.deleteOnExit();
        try (FileWriter fw = new FileWriter(tmp)) {
            fw.write("file-content");
        }

        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        when(servletResponse.getHeaders(any())).thenReturn(Collections.emptyList());
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(jakarta.servlet.WriteListener l) {}
            @Override public void write(int b) { captured.write(b); }
            @Override public void write(byte[] b, int off, int len) { captured.write(b, off, len); }
        };
        when(servletResponse.getOutputStream()).thenReturn(sos);

        HttpResponse response = builder(HttpResponse.of(tmp))
                .set(HttpResponse::setHeaders, Headers.empty())
                .build();

        ServletUtils.updateServletResponse(servletResponse, response);

        assertThat(captured.toString(StandardCharsets.UTF_8)).isEqualTo("file-content");
    }

    @Test
    void updateServletResponseSetsStringHeader() throws IOException {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        // Headers.keySet() normalizes "AAA" → "Aaa"
        when(servletResponse.getHeaders("Aaa")).thenReturn(Collections.emptyList());
        when(servletResponse.getWriter()).thenReturn(mock(PrintWriter.class));

        HttpResponse response = builder(HttpResponse.of(""))
                .set(HttpResponse::setHeaders, Headers.of("AAA", "val"))
                .build();

        ServletUtils.updateServletResponse(servletResponse, response);

        verify(servletResponse).setHeader("Aaa", "val");
    }

    @Test
    void updateServletResponseSetsNumericHeader() throws IOException {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        // Headers.keySet() normalizes "AAA" → "Aaa"
        when(servletResponse.getHeaders("Aaa")).thenReturn(Collections.emptyList());
        when(servletResponse.getWriter()).thenReturn(mock(PrintWriter.class));

        HttpResponse response = builder(HttpResponse.of(""))
                .set(HttpResponse::setHeaders, Headers.of("AAA", 1))
                .build();

        ServletUtils.updateServletResponse(servletResponse, response);

        verify(servletResponse).setHeader("Aaa", "1");
    }

    @Test
    void updateServletResponseDoesNothingForNullArguments() {
        assertThatCode(() -> {
            ServletUtils.updateServletResponse(null, mock(HttpResponse.class));
            ServletUtils.updateServletResponse(mock(HttpServletResponse.class), null);
        }).doesNotThrowAnyException();
    }

    @Test
    void setBodyThrowsUnreachableExceptionForUnsupportedType() {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getHeaders()).thenReturn(Headers.empty());
        when(response.getBody()).thenReturn(12345);

        // UnreachableException is a RuntimeException, not IOException,
        // so it propagates directly without being wrapped in FalteringEnvironmentException
        assertThatThrownBy(() -> ServletUtils.updateServletResponse(servletResponse, response))
                .isInstanceOf(UnreachableException.class);
    }
}
