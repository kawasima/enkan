package enkan.data;

import javax.ws.rs.core.MediaType;

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
}
