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
        Multimap<String, Object> errors = (Multimap<String, Object>) getExtension("errors");
        return errors != null && !errors.isEmpty();
    }

    default boolean hasErrors(String key) {
        return ThreadingUtils.some((Multimap<String, Object>) getExtension("errors"),
                errors -> errors.getAll(key),
                errors -> !errors.isEmpty()).orElse(false);
    }

    default List<Object> getErrors(String key) {
        return getErrors().getAll(key);
    }

    default Multimap<String, Object> getErrors() {
        return (Multimap<String, Object>) getExtension("errors");
    }

    default void setErrors(Multimap<String, Object> errors) {
        setExtension("errors", errors);
    }
}
