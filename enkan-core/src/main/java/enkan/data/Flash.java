package enkan.data;

import java.io.Serializable;

/**
 * A one-time message carrier that survives exactly one redirect.
 *
 * <p>Flash values are written to the session by the current request handler
 * and consumed (and removed) by the next request.  This makes them ideal for
 * post-redirect-get patterns such as success or error notifications.
 *
 * @param <T>   the type of the carried value; must be {@link Serializable}
 *              so that the flash survives session serialization
 * @param value the payload to carry to the next request
 * @author kawasima
 */
public record Flash<T extends Serializable>(T value) implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Returns the payload stored in this flash.
     *
     * @return the flash value
     */
    @Override
    public T value() {
        return value;
    }
}
