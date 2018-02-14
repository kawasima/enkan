package kotowari.data;

import enkan.data.Extendable;

/**
 * Binds the http parameters to a specified form object.
 *
 * @author kawasima
 */
public interface BodyDeserializable extends Extendable {
    default <T> T getDeserializedBody() {
        return getExtension("deserializedBody");
    }

    default <T> void setDeserializedBody(T obj) {
        setExtension("deserializedBody", obj);
    }
}
