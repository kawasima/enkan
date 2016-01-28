package enkan.util;

import sun.util.ResourceBundleEnumeration;

import java.util.*;

/**
 * @author kawasima
 */
public class MergeableResourceBundle extends ResourceBundle {
    private Map<String, Object> lookup;

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
        return new ResourceBundleEnumeration(lookup.keySet(),
                (parent != null) ? parent.getKeys() : null);
    }
}
