package enkan.data;

import java.io.Serializable;

/**
 * @author kawasima
 */
public interface FlashAvailable<T extends Serializable> extends Extendable {
    default Flash<T> getFlash() {
        return (Flash<T>) getExtension("flash");
    }

    default void setFlash(Flash<T> flash) {
        setExtension("flash", flash);
    }
}
