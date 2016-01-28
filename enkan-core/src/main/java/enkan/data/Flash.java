package enkan.data;

import java.io.Serializable;

/**
 * @author kawasima
 */
public class Flash<T extends Serializable> implements Serializable {
    private final T value;

    public Flash(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
