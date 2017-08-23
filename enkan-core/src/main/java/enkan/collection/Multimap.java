package enkan.collection;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class Multimap<K, V> implements Map<K, V> {
    private HashMap<K, List<V>> hashMap;

    private Multimap() {
        hashMap = new HashMap<>();
    }

    public static Multimap empty() {
        return new Multimap<>();
    }

    public static <K, V> Multimap of(K k1, V v1) {
        Multimap<K, V> m = new Multimap<>();
        m.put(k1, v1);
        return m;
    }

    public static <K, V> Multimap of(K k1, V v1, K k2, V v2) {
        Multimap<K, V> m = Multimap.of(k1, v1);
        m.put(k2, v2);
        return m;
    }

    public static <K, V> Multimap of(K k1, V v1, K k2, V v2, K k3, V v3) {
        Multimap<K, V> m = Multimap.of(k1, v1, k2, v2);
        m.put(k3, v3);
        return m;
    }

    public static <K, V> Multimap of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Multimap<K, V> m = Multimap.of(k1, v1, k2, v2, k3, v3);
        m.put(k4, v4);
        return m;
    }

    @Override
    public int size() {
        return hashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return hashMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return hashMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        List<V> values = hashMap.get(key);
        if (values == null || values.isEmpty())
            return null;

        return values.get(0);
    }

    public List<V> getAll(K key) {
        return hashMap.get(key);
    }

    public void add(K key, V value) {
        if (hashMap.containsKey(key)) {
            List<V> values = hashMap.get(key);
            values.add(value);
        } else {
            List<V> values = new ArrayList<>();
            values.add(value);
            hashMap.put(key, values);
        }
    }

    @Override
    public V put(K key, V value) {
        List<V> values = new ArrayList<>();
        values.add(value);
        hashMap.put(key, values);
        return value;
    }

    @Override
    public V remove(Object key) {
        List<V> old = hashMap.remove(key);
        if (old != null && !old.isEmpty()) {
            return old.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        hashMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return hashMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return hashMap.values()
                .stream()
                .map(vs -> (vs != null && !vs.isEmpty()) ? vs.get(0) : null)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return hashMap.entrySet()
                .stream()
                .map(e -> new Map.Entry<K, V>() {
                    @Override
                    public K getKey() {
                        return e.getKey();
                    }

                    @Override
                    public V getValue() {
                        List<V> values = e.getValue();
                        return (values != null && !values.isEmpty()) ? values.get(0) : null;
                    }

                    @Override
                    public V setValue(V value) {
                        throw new UnsupportedOperationException("Map.Entry#setValue");
                    }
                })
                .collect(Collectors.toSet());
    }

    public void replaceEachValues(K key, Function<V, V> replacer) {
        List<V> values = getAll(key);
        hashMap.put(key, values.stream().map(replacer).collect(Collectors.toList()));
    }
}
