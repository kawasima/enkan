package enkan.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * The utility for formatting HTTP Date.
 *
 * @author kawasima
 */
public enum HttpDateFormat {
    RFC822("EEE, dd MMM yyyy HH:mm:ss Z"),
    RFC1123("EEE, dd MMM yyyy HH:mm:ss zzz"),
    RFC1036("EEEE, dd-MMM-yy HH:mm:ss zzz"),
    ASCTIME("EEE MMM d HH:mm:ss yyyy");

    private String formatStr;

    HttpDateFormat(String formatStr) {
        this.formatStr = formatStr;
    }

    private DateFormat formatter() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatStr, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat;
    }

    public String format(Date d) {
        return formatter().format(d);
    }
}
