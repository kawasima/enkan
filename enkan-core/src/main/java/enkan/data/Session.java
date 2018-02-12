package enkan.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Data that have the lifecycle between multiple requests.
 *
 * This is not dependent on javax.servlet.HttpSession.
 *
 * @author kawasima
 */
@SuppressWarnings("NullableProblems")
public class Session implements Map<String, Serializable>, Serializable {
    /** Holds the attributes of session **/
    private final Map<String, Serializable> attrs;
    private boolean isNew = true;

    public Session() {
        attrs = new HashMap<>();
    }

    /**
     * Mark that persisted into the session store.
     */
    public void persist() {
        isNew = false;
    }
    public boolean isNew() {
        return isNew;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return attrs.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return attrs.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return attrs.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        return attrs.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable get(Object key) {
        return attrs.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable put(String key, Serializable value) {
        return attrs.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable remove(Object key) {
        return attrs.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map<? extends String, ? extends Serializable> m) {
        attrs.putAll(m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        attrs.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> keySet() {
        return attrs.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Serializable> values() {
        return attrs.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<String, Serializable>> entrySet() {
        return attrs.entrySet();
    }
}
