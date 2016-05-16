package enkan.data;

import java.io.Serializable;

/**
 * Holds a flashing value.
 *
 * @author kawasima
 */
public class Flash<T extends Serializable> implements Serializable {
    private final T value;

    public Flash(T value) {
        this.value = value;
    }

    /**
     * Gets the flashing value.
     *
     * @return a flashing value
     */
    public T getValue() {
        return value;
    }
}
