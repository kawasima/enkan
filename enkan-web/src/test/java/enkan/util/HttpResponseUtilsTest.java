package enkan.util;

import enkan.collection.Headers;
import enkan.data.DefaultHttpResponse;
import enkan.data.HttpResponse;
import org.junit.Test;

import static enkan.util.BeanBuilder.builder;
import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class HttpResponseUtilsTest {
    @Test
    public void charset() {
        HttpResponse response = builder(HttpResponse.of("for testing"))
                .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/html"))
                .build();
        HttpResponseUtils.charset(response, "ISO_8859_1");
        assertEquals("text/html; charset=ISO_8859_1", response.getHeaders().get("Content-Type"));
    }

    @Test
    public void charsetWithoutContentType() {
        HttpResponse response = HttpResponse.of("for testing");
        response.getHeaders().remove("Content-Type");
        HttpResponseUtils.charset(response, "ISO_8859_1");
        assertEquals("text/plain; charset=ISO_8859_1",
                response.getHeaders().get("Content-Type"));
    }


}