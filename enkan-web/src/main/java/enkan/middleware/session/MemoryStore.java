package enkan.middleware.session;

import enkan.data.Session;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kawasima
 */
public class MemoryStore implements SessionStore {
    private ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();


    @Override
    public Session read(String key) {
        return sessionMap.get(key);
    }

    @Override
    public String write(String key, Session session) {
        if (key == null) {
            key = UUID.randomUUID().toString();
        }

        sessionMap.putIfAbsent(key, session);
        return key;
    }

    @Override
    public String delete(String key) {
        sessionMap.remove(key);
        return null;
    }
}
