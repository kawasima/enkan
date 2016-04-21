package kotowari.data;

import enkan.data.Extendable;

import java.io.Serializable;

/**
 * Binds the http parameters to a specified form object.
 *
 * @author kawasima
 */
public interface BodyDeserializable extends Extendable {
    default <T> T getDeserializedBody() {
        return (T) getExtension("deserializedBody");
    }

    default <T> void setDeserializedBody(T obj) {
        setExtension("deserializedBody", obj);
    }
}
