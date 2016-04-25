package enkan.data;

import javax.ws.rs.core.MediaType;
import java.util.Locale;

/**
 * @author kawasima
 */
public interface ContentNegotiable extends Extendable {
    default MediaType getAccept() {
        return (MediaType) getExtension("accept");
    }

    default void setAccept(MediaType mediaType) {
        setExtension("accept", mediaType);
    }

    default Locale getAcceptLanguage() {
        return (Locale) getExtension("acceptLanguage");
    }

    default void setAcceptLanguage(Locale locale) {
        setExtension("acceptLanguage", locale);
    }
}
