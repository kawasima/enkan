package enkan.collection;

import java.util.*;

/**
 * A map implementation for options.
 *
 * @author kawasima
 */
public class OptionMap extends HashMap<String, Object> {
    public static OptionMap empty() {
        return new OptionMap();
    }

    public static OptionMap of(OptionMap init) {
        OptionMap m = empty();
        init.forEach(m::put);
        return m;
    }

    public static OptionMap of(Object... init) {
        OptionMap m = empty();
        for(int i = 0; i < init.length; i += 2) {
            m.put(init[i].toString(), init[i + 1]);
        }
        return m;
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        Object value = get(key);
        if (value == null) return defaultValue;
        return value.toString();
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        Object value = get(key);
        if (value == null) return defaultValue;

        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            return Integer.parseInt(value.toString());
        }
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }

    public long getLong(String key, long defaultValue) {
        Object value = get(key);
        if (value == null) return defaultValue;

        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else {
            return Integer.parseInt(value.toString());
        }
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, true);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = get(key);
        if (value == null) return defaultValue;

        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return getInt(key) != 0;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Object> getList(String key) {
        Object value = this.get(key);
        if (value == null) {
            return new ArrayList<>();
        }
        List<Object> valueList;
        if (List.class.isAssignableFrom(value.getClass())) {
            valueList = (List<Object>) value;
        } else if (value.getClass().isArray()) {
            valueList = Arrays.asList((Object[])value);
        } else if (Collection.class.isAssignableFrom(value.getClass())) {
            valueList = new ArrayList<>(Collection.class.cast(value));
        } else {
            valueList = new ArrayList<>(1);
            valueList.add(value);
        }
        return valueList;
    }
}
