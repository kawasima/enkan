package enkan.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author kawasima
 */
public final class PersistentMarkedSession extends Session {
    public boolean isNew() {
        return false;
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("size");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("isEmpty");
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException("containsKey");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("containsValue");
    }

    @Override
    public Serializable get(Object key) {
        throw new UnsupportedOperationException("get");
    }

    @Override
    public Serializable put(String key, Serializable value) {
        throw new UnsupportedOperationException("put");
    }

    @Override
    public Serializable remove(Object key) {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Serializable> m) {
        throw new UnsupportedOperationException("putAll");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear");
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("keySet");
    }

    @Override
    public Collection<Serializable> values() {
        throw new UnsupportedOperationException("values");
    }

    @Override
    public Set<Entry<String, Serializable>> entrySet() {
        throw new UnsupportedOperationException("entrySet");
    }
}
