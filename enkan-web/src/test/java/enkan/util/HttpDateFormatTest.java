package enkan.util;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class HttpDateFormatTest {
    @Test
    public void test() {
        Date d = new Date(1234556789012l);
        assertEquals("Fri, 13 Feb 2009 20:26:29 +0000", HttpDateFormat.RFC822.format(d));
        assertEquals("Fri Feb 13 20:26:29 2009", HttpDateFormat.ASCTIME.format(d));
        assertEquals("Friday, 13-Feb-09 20:26:29 GMT", HttpDateFormat.RFC1036.format(d));
        assertEquals("Fri, 13 Feb 2009 20:26:29 GMT", HttpDateFormat.RFC1123.format(d));
    }
}