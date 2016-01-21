package kotowari.data;

import enkan.data.Extendable;

import java.io.Serializable;

/**
 * @author kawasima
 */
public interface FormAvailable extends Extendable {
    default Serializable getForm() {
        return (Serializable) getExtension("form");
    }

    default void setForm(Serializable form) {
        setExtension("form", form);
    }
}
