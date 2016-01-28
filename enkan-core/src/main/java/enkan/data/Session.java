package enkan.data;

import enkan.annotation.Middleware;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kawasima
 */
public class Session implements Serializable {
    private Map<String, Object> sessionMap;
    private boolean valid = true;

    public <T> T getAttribute(String name) {
        return sessionMap == null ? null : (T) sessionMap.get(name);
    }

    public <T> void setAttribute(String name, T value) {
        if (sessionMap == null) {
            sessionMap = new HashMap<>();
        }
        sessionMap.put(name, value);
    }

    public void removeAttribute(String name) {
        if (sessionMap != null) {
            sessionMap.remove(name);
        }
    }

    public void invalidate() {
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }
}
