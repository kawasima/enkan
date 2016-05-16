package enkan.middleware.session;

import enkan.exception.FalteringEnvironmentException;
import enkan.exception.MisconfigurationException;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kawasima
 */
public class MemoryStore implements KeyValueStore {
    private ConcurrentHashMap<String, byte[]> sessionMap = new ConcurrentHashMap<>();


    @Override
    public Serializable read(String key) {
        byte[] buf = sessionMap.get(key);
        if (buf == null) return null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(buf);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Serializable) ois.readObject();
        } catch (IOException ex) {
            throw new FalteringEnvironmentException(ex);
        } catch (ClassNotFoundException e) {
            throw new MisconfigurationException("CLASS_NOT_FOUND", e);
        }
    }

    @Override
    public String write(String key, Serializable value) {
        if (key == null) {
            key = UUID.randomUUID().toString();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(value);
            sessionMap.put(key, baos.toByteArray());
        } catch (IOException ex) {
            throw new FalteringEnvironmentException(ex);
        }

        return key;
    }

    @Override
    public String delete(String key) {
        sessionMap.remove(key);
        return null;
    }
}
