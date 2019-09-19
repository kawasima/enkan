package enkan.util;

import enkan.collection.Headers;
import enkan.data.HttpResponse;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static enkan.util.BeanBuilder.builder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author kawasima
 */
class ServletUtilsTest {
    @Test
    void test() throws IOException {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(servletResponse.getWriter()).thenReturn(writer);
        HttpResponse response = builder(HttpResponse.of(""))
                .set(HttpResponse::setHeaders, Headers.of("AAA", 1))
                .build();
        ServletUtils.updateServletResponse(servletResponse, response);
    }
}
