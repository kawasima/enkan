package kotowari.example.form;

import kotowari.data.Validatable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kawasima
 */
public class FormBase implements Validatable, Serializable {
    private Map<String, Object> extensions = new HashMap<>();
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getExtension(String name) {
        return (T) extensions.get(name);
    }

    @Override
    public <T> void setExtension(String name, T extension) {
        extensions.put(name, extension);
    }
}
