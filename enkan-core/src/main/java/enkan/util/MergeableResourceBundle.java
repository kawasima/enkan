package enkan.util;

import java.util.*;

/**
 * @author kawasima
 */
public class MergeableResourceBundle extends ResourceBundle {
    private final Map<String, Object> lookup;

    protected MergeableResourceBundle(Properties properties) {
        lookup = new HashMap(properties);
    }

    @Override
    protected Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return lookup.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        ResourceBundle parent = this.parent;
        Enumeration<String> enumeration = (parent != null) ? parent.getKeys() : null;
        return new Enumeration<String>() {
            private final Iterator<String> iterator = lookup.keySet().iterator();
            private String next = null;

            @Override
            public boolean hasMoreElements() {
                if (next == null) {
                    if (iterator.hasNext()) {
                        next = iterator.next();
                        while(next == null && enumeration.hasMoreElements()) {
                            next = enumeration.nextElement();
                            if (lookup.keySet().contains(next)) {
                                next = null;
                            }
                        }
                    }
                }
                return next != null;
            }

            @Override
            public String nextElement() {
                if (hasMoreElements()) {
                    String result = next;
                    next = null;
                    return result;
                }
                throw new NoSuchElementException();
            }
        };
    }
}
