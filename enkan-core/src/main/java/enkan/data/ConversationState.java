package enkan.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author kawasima
 */
public class ConversationState implements Map<String, Object>, Serializable {
    private Map<String, Object> attrs = new HashMap<>();

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
    public Object get(Object key) {
        return attrs.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return attrs.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return attrs.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
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
    public Collection<Object> values() {
        return attrs.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return attrs.entrySet();
    }
}

