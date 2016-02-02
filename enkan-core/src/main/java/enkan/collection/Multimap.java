package enkan.collection;

import java.util.*;

/**
 * @author kawasima
 */
public class Multimap<K, V> {
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

    public int size() {
        return hashMap.size();
    }

    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    public boolean containsKey(Object key) {
        return hashMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return hashMap.containsValue(value);
    }

    public V get(Object key) {
        List<V> values = hashMap.get(key);
        if (values == null || values.isEmpty())
            return null;

        return values.get(0);
    }

    public List<V> getAll(Object key) {
        return hashMap.get(key);
    }

    public void put(K key, V value) {
        if (hashMap.containsKey(key)) {
            List<V> values = hashMap.get(key);
            values.add(value);
        } else {
            List<V> values = new ArrayList<>();
            values.add(value);
            hashMap.put(key, values);
        }
    }

    public List<V> remove(Object key) {
        return hashMap.remove(key);
    }

    public void putAll(Map<? extends K, ? extends List<V>> m) {
        hashMap.putAll(m);
    }

    public void clear() {
        hashMap.clear();
    }

    public Set<K> keySet() {
        return hashMap.keySet();
    }

    public Collection<List<V>> values() {
        return hashMap.values();
    }

    public Set<Map.Entry<K, List<V>>> entrySet() {
        return hashMap.entrySet();
    }
}
