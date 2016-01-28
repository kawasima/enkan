package enkan.data;

/**
 * @author kawasima
 */
public interface WebSessionAvailable extends SessionAvailable, Extendable {
    default String getSessionKey() {
        Object key = getExtension("session/key");
        return key != null ? key.toString() : null;
    }

    default void setSessionKey(String key) {
        setExtension("session/key", key);
    }
}
