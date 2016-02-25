package enkan.collection;

import java.util.*;

/**
 * @author kawasima
 */
public class Parameters implements Map<String, Object> {
    private HashMap<String, Object> params = new HashMap<>();
    private boolean caseSensitive = true;

    protected Parameters() {

    }

    protected void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    public static Parameters empty() {
        return new Parameters();
    }


    public static Parameters of(String k1, Object v1) {
        Parameters params = empty();
        params.put(k1, v1);
        return params;
    }

    public static Parameters of(String k1, Object v1, String k2, Object v2) {
        Parameters params = Parameters.of(k1, v1);
        params.put(k2, v2);
        return params;
    }

    public static Parameters of(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
        Parameters params = Parameters.of(k1, v1, k2, v2);
        params.put(k3, v3);
        return params;
    }

    public static Parameters of(String k1, Object v1, String k2, Object v2, String k3, Object v3,  String k4, Object v4) {
        Parameters params = Parameters.of(k1, v1, k2, v2, k3, v3);
        params.put(k4, v4);
        return params;
    }

    public static Parameters of(Object... init) {
        Parameters params = Parameters.empty();
        for(int i = 0; i < init.length; i += 2) {
            params.put(init[i].toString(), init[i + 1]);
        }
        return params;
    }

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
        if (!caseSensitive && String.class.isInstance(key)) {
            key = String.class.cast(key).toLowerCase(Locale.US);
        }
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
    public String get(Object key) {
        if (!caseSensitive && String.class.isInstance(key)) {
            key = String.class.cast(key).toLowerCase(Locale.US);
        }
        Object val = params.get(key);
        if (val == null) return null;
        return val.toString();
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
        Object current = getRawType(key);

        while (current != null && idx < keys.length) {
            if (keys[idx] == null) return null;

            if (current instanceof List) {
                List<?> list = List.class.cast(current);
                Integer i = keyToInt(keys[idx]);
                if (i == null || i < 0 || i >= list.size()) return null;

                current = list.get(i);
            } else if (current instanceof Parameters) {
                Parameters map = Parameters.class.cast(current);
                current = map.getRawType(keys[idx]);
            } else {
                return null;
            }
            idx++;
        }

        return current;
    }

    public Object getRawType(Object key) {
        if (!caseSensitive && String.class.isInstance(key)) {
            key = String.class.cast(key).toLowerCase(Locale.US);
        }

        return params.get(key);
    }

    public <T> List<T> getList(Object key, Object... keys) {
        T value = (T) getIn(key, keys);
        if (value == null) {
            return new ArrayList<>();
        } else if (value instanceof List) {
            return List.class.cast(value);
        } else {
            List<T> values = new ArrayList<>();
            values.add(value);
            return values;
        }
    }

    public Long getLong(Object key, Object... keys) {
        Object value = getIn(key, keys);
        if (value == null) {
            return null;
        } else {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    @Override
    public Object put(String key, Object value) {
        if (!caseSensitive) {
            key = key.toLowerCase(Locale.US);
        }
        Object v = params.get(key);
        if (v == null) {
            params.put(key, value);
        } else if (v instanceof List) {
            ((List<Object>)v).add(value);
        } else {
            List<Object> values = new ArrayList<>();
            values.add(v);
            values.add(value);
            params.put(key, values);
        }
        return v;
    }

    @Override
    public Object remove(Object key) {
        return params.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        params.putAll(m);
    }

    @Override
    public Object replace(String key, Object value) {
        return params.replace(key, value);
    }

    @Override
    public void clear() {
        params.clear();
    }

    @Override
    public Set<String> keySet() {
        return params.keySet();
    }

    @Override
    public Collection<Object> values() {
        return params.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return params.entrySet();
    }

    @Override
    public String toString() {
        return params.toString();
    }

}
