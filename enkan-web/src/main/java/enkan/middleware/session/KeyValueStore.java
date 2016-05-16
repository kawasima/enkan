package enkan.middleware.session;

import java.io.Serializable;

/**
 * @author kawasima
 */
public interface KeyValueStore {
    /**
     * Read the value.
     *
     * @param key a String contains a store key
     * @return a stored object
     */
    Serializable read(String key);

    /**
     * Write the value with the given key.
     *
     * @param key a String contains a store key
     * @param value a stored object
     * @return new store key
     */
    String write(String key, Serializable value);

    /**
     * Delete the key and the value.
     *
     * @param key a String contains a store key
     * @return a String contains a store key
     */
    String delete(String key);
}
