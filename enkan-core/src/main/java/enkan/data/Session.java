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
public class Session implements Map<String, Serializable>, Serializable {
    /** Holds the attributes of session **/
    private Map<String, Serializable> attrs;
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

    @Override
    public int size() {
        return attrs.size();
    }

    @Override
    public boolean isEmpty() {
        return attrs.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return attrs.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return attrs.containsValue(value);
    }

    @Override
    public Serializable get(Object key) {
        return attrs.get(key);
    }

    @Override
    public Serializable put(String key, Serializable value) {
        return attrs.put(key, value);
    }

    @Override
    public Serializable remove(Object key) {
        return attrs.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Serializable> m) {
        attrs.putAll(m);
    }

    @Override
    public void clear() {
        attrs.clear();
    }

    @Override
    public Set<String> keySet() {
        return attrs.keySet();
    }

    @Override
    public Collection<Serializable> values() {
        return attrs.values();
    }

    @Override
    public Set<Entry<String, Serializable>> entrySet() {
        return attrs.entrySet();
    }
}
