package kotowari.data;

import enkan.collection.Multimap;
import enkan.data.Extendable;

import java.util.List;

/**
 * @author kawasima
 */
public interface Validatable extends Extendable {
    default boolean hasErrors() {
        Multimap<String, String> errors = (Multimap<String, String>) getExtension("errors");
        if (errors != null) {
            return !errors.isEmpty();
        }
        return false;
    }

    default boolean hasErrors(String key) {
        Multimap<String, String> errors = (Multimap<String, String>) getExtension("errors");
        if (errors != null) {
            return !errors.getAll(key).isEmpty();
        }
        return false;
    }

    default List<String> getErrors(String key) {
        return getErrors().getAll(key);
    }

    default Multimap<String, String> getErrors() {
        return (Multimap<String, String>) getExtension("errors");
    }

    default void setErrors(Multimap<String, String> errors) {
        setExtension("errors", errors);
    }
}
