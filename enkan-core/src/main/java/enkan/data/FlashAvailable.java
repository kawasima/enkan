package enkan.data;

/**
 * @author kawasima
 */
public interface FlashAvailable extends Extendable {
    default Flash<?> getFlash() {
        return getExtension("flash");
    }

    default void setFlash(Flash<?> flash) {
        setExtension("flash", flash);
    }
}
