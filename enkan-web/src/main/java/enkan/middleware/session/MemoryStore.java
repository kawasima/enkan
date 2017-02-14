package enkan.middleware.session;

import enkan.exception.FalteringEnvironmentException;
import enkan.exception.MisconfigurationException;

import java.io.*;
import java.lang.reflect.Proxy;
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
                throws IOException, ClassNotFoundException {

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
