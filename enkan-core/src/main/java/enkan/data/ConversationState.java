package enkan.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * State object in conversation scope.
 *
 * @author kawasima
 */
@SuppressWarnings("NullableProblems")
public class ConversationState implements Map<String, Object>, Serializable {
    private final Map<String, Object> attrs = new HashMap<>();

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
    public Object get(Object key) {
        return attrs.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(String key, Object value) {
        return attrs.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object remove(Object key) {
        return attrs.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map<? extends String, ?> m) {
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
    public Collection<Object> values() {
        return attrs.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return attrs.entrySet();
    }
}

