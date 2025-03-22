package enkan.data;

import java.io.Serializable;

/**
 * Holds a flashing value.
 *
 * @author kawasima
 */
public record Flash<T extends Serializable>(T value) implements Serializable {

    /**
     * Gets the flashing value.
     *
     * @return a flashing value
     */
    @Override
    public T value() {
        return value;
    }
}
