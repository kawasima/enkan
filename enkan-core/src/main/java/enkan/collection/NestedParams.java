package enkan.collection;

import java.util.*;

/**
 * @author kawasima
 */
public abstract class NestedParams<T> implements Map<T, Object> {
    private HashMap<T, Object> params = new HashMap<>();

    /**
     *
     * @return {@inheritDoc}
     */
    @Override
    public int size() {
        return params.size();
    }

    /**
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return params.isEmpty();
    }

    /**
     *
     * @param key {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return params.containsKey(key);
    }

    /**
     *
     * @param value {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        return params.containsValue(value);
    }

    /**
     *
     * @param key {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Object get(Object key) {
        return params.get(key);
    }

    private Integer keyToInt(Object key) {
        Integer i;
        if (key instanceof Number) {
            i = ((Number) key).intValue();
        } else {
            try {
                i = Integer.parseInt((key.toString()));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return i;
    }

    /**
     * Get a nested value.
     *
     * @param key first key
     * @param keys rest keys
     * @return found object
     */
    public Object getIn(Object key, Object... keys) {
        int idx = 0;
        Object current = get(key);

        while (current != null && idx < keys.length) {
            if (keys[idx] == null) return null;

            if (current instanceof List) {
                List<?> list = List.class.cast(current);
                Integer i = keyToInt(keys[idx]);
                if (i == null || i < 0 || i >= list.size()) return null;

                current = list.get(i);
            } else if(current instanceof VectorNestedParams) {
                VectorNestedParams vec = VectorNestedParams.class.cast(current);
                Integer i = keyToInt(keys[idx]);
                if (i == null || i < 0 || i >= vec.size()) return null;

                current = vec.get(i);
            } else if (current instanceof Map) {
                Map<?, ?> map = Map.class.cast(current);
                current = map.get(keys[idx]);
            } else {
                return null;
            }
            idx++;
        }

        return current;
    }

    @Override
    public Object put(T key, Object value) {
        return params.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return params.remove(key);
    }

    @Override
    public void putAll(Map<? extends T, ?> m) {
        params.putAll(m);
    }

    @Override
    public void clear() {
        params.clear();
    }

    @Override
    public Set<T> keySet() {
        return params.keySet();
    }

    @Override
    public Collection<Object> values() {
        return params.values();
    }

    @Override
    public Set<Entry<T, Object>> entrySet() {
        return params.entrySet();
    }

    @Override
    public String toString() {
        return params.toString();
    }
}
