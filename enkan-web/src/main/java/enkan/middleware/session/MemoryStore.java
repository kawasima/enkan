package enkan.middleware.session;

import enkan.exception.FalteringEnvironmentException;
import enkan.exception.MisconfigurationException;

import java.io.*;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author kawasima
 */
public class MemoryStore implements KeyValueStore, Closeable {
    private static final long DEFAULT_TTL_SECONDS = 1800L;
    private static final long PURGE_INTERVAL_SECONDS = 60L;

    private record Entry(byte[] data, long expiresAt) {}

    private final ConcurrentHashMap<String, Entry> sessionMap = new ConcurrentHashMap<>();
    private long ttlSeconds = DEFAULT_TTL_SECONDS;
    private final ScheduledExecutorService purgeScheduler;

    public MemoryStore() {
        purgeScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "enkan-memory-store-purge");
            t.setDaemon(true);
            return t;
        });
        purgeScheduler.scheduleAtFixedRate(this::purgeExpired,
                PURGE_INTERVAL_SECONDS, PURGE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void purgeExpired() {
        long now = System.currentTimeMillis();
        sessionMap.entrySet().removeIf(e -> now > e.getValue().expiresAt());
    }

    @Override
    public void close() {
        purgeScheduler.shutdown();
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public Serializable read(String key) {
        Entry entry = sessionMap.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expiresAt()) {
            sessionMap.remove(key);
            return null;
        }
        byte[] buf = entry.data();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(buf);
             ObjectInputStream ois = new CustomObjectInputStream(bais, Thread.currentThread().getContextClassLoader())) {
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
            long expiresAt = System.currentTimeMillis() + ttlSeconds * 1000L;
            sessionMap.put(key, new Entry(baos.toByteArray(), expiresAt));
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

    /**
     * Custom subclass of <code>ObjectInputStream</code> that loads from the
     * class loader for this enkan application.  This allows classes defined only
     * with the web application to be found correctly.
     *
     * @author Craig R. McClanahan
     * @author Bip Thelin
     */
    private static class CustomObjectInputStream extends ObjectInputStream {
        /**
         * The class loader we will use to resolve classes.
         */
        private final ClassLoader classLoader;


        /**
         * Construct a new instance of CustomObjectInputStream
         *
         * @param stream The input stream we will read from
         * @param classLoader The class loader used to instantiate objects
         *
         * @exception IOException if an input/output error occurs
         */
        public CustomObjectInputStream(InputStream stream,
                                       ClassLoader classLoader)
                throws IOException {

            super(stream);
            this.classLoader = classLoader;
        }


        /**
         * Load the local class equivalent of the specified stream class
         * description, by using the class loader assigned to this Context.
         *
         * @param classDesc Class description from the input stream
         *
         * @exception ClassNotFoundException if this class cannot be found
         * @exception IOException if an input/output error occurs
         */
        @Override
        public Class<?> resolveClass(ObjectStreamClass classDesc)
                throws ClassNotFoundException, IOException {
            try {
                return Class.forName(classDesc.getName(), false, classLoader);
            } catch (ClassNotFoundException e) {
                try {
                    // Try also the superclass because of primitive types
                    return super.resolveClass(classDesc);
                } catch (ClassNotFoundException e2) {
                    // Rethrow original exception, as it can have more information
                    // about why the class was not found. BZ 48007
                    throw e;
                }
            }
        }


        /**
         * Return a proxy class that implements the interfaces named in a proxy
         * class descriptor. Do this using the class loader assigned to this
         * Context.
         */
        @Override
        protected Class<?> resolveProxyClass(String[] interfaces)
                throws ClassNotFoundException {

            Class<?>[] cinterfaces = new Class[interfaces.length];
            for (int i = 0; i < interfaces.length; i++)
                cinterfaces[i] = classLoader.loadClass(interfaces[i]);

            try {
                return Proxy.getProxyClass(classLoader, cinterfaces);
            } catch (IllegalArgumentException e) {
                throw new ClassNotFoundException(null, e);
            }
        }
    }
}
