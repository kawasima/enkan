package kotowari.data;

import enkan.collection.Multimap;
import enkan.data.Extendable;
import enkan.util.ThreadingUtils;

import java.util.List;

/**
 * @author kawasima
 */
public interface Validatable extends Extendable {
    default boolean hasErrors() {
        Multimap<String, String> errors = (Multimap<String, String>) getExtension("errors");
        return errors != null && !errors.isEmpty();
    }

    default boolean hasErrors(String key) {
        return ThreadingUtils.some((Multimap<String, String>) getExtension("errors"),
                errors -> errors.getAll(key),
                errors -> !errors.isEmpty()).orElse(false);
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
