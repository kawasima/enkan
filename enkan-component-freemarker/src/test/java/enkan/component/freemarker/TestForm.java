package enkan.component.freemarker;

import enkan.collection.Multimap;
import kotowari.data.Validatable;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple Validatable implementation for testing.
 */
public class TestForm implements Validatable {
    private String name;
    private final Map<String, Object> extensions = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtension(String name) {
        return (T) extensions.get(name);
    }

    @Override
    public <T> void setExtension(String name, T extension) {
        extensions.put(name, extension);
    }

    public void addError(String field, String message) {
        Multimap<String, Object> errors = getExtension("errors");
        if (errors == null) {
            errors = Multimap.empty();
            setExtension("errors", errors);
        }
        errors.add(field, message);
    }
}
