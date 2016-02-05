package enkan.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author kawasima
 */
public class VectorNestedParams extends NestedParams<Integer> {
    public void add(Object value) {
        put(size(), value);
    }

    public void addAll(Iterable<?> values) {
        values.forEach(v -> add(v));
    }

    @Override
    public Set<Integer> keySet() {
        LinkedHashSet<Integer> keyset = new LinkedHashSet<>(size());
        for (int i = 0; i < size(); i++) {
            keyset.add(i);
        }
        return keyset;
    }

    @Override
    public Collection<Object> values() {
        LinkedHashSet<Object> values = new LinkedHashSet<>(size());
        for (int i = 0; i < size(); i++) {
            values.add(i);
        }
        return values;
    }

    @Override
    public String toString() {
        return new ArrayList(super.values()).toString();
    }
}
