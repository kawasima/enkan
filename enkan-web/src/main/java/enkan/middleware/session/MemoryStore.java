package enkan.middleware.session;

import enkan.data.Session;
import enkan.exception.FalteringEnvironmentException;
import enkan.exception.UnreachableException;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kawasima
 */
public class MemoryStore implements SessionStore {
    private ConcurrentHashMap<String, byte[]> sessionMap = new ConcurrentHashMap<>();


    @Override
    public Session read(String key) {
        byte[] buf = sessionMap.get(key);
        if (buf == null) return null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(buf);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Session) ois.readObject();
        } catch (IOException ex) {
            throw FalteringEnvironmentException.create(ex);
        } catch (ClassNotFoundException e) {
            throw UnreachableException.create(e);
        }
    }

    @Override
    public String write(String key, Session session) {
        if (key == null) {
            key = UUID.randomUUID().toString();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(session);
            sessionMap.put(key, baos.toByteArray());
        } catch (IOException ex) {
            throw FalteringEnvironmentException.create(ex);
        }

        return key;
    }

    @Override
    public String delete(String key) {
        sessionMap.remove(key);
        return null;
    }
}
