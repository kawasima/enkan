package enkan.util;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
class HttpDateFormatTest {
    @Test
    void test() {
        Date d = new Date(1234556789012L);
        assertThat(HttpDateFormat.RFC822.format(d))
                .isEqualTo("Fri, 13 Feb 2009 20:26:29 +0000");
        assertThat(HttpDateFormat.ASCTIME.format(d))
                .isEqualTo("Fri Feb 13 20:26:29 2009");
        assertThat(HttpDateFormat.RFC1036.format(d))
                .isEqualTo("Friday, 13-Feb-09 20:26:29 GMT");
        assertThat(HttpDateFormat.RFC1123.format(d))
                .isEqualTo("Fri, 13 Feb 2009 20:26:29 GMT");
    }
}
