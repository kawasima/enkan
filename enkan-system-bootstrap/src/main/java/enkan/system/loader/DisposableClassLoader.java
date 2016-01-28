package enkan.system.loader;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author kawasima
 */
public class DisposableClassLoader extends URLClassLoader {
    private File baseDir;

    DisposableClassLoader(URL url, ClassLoader parent) {
        super(new URL[]{ url }, parent);
        try {
            baseDir = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Class defineClass(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }

    private Class defineClass(String name, boolean resolve) {
        try (InputStream in = new FileInputStream(new File(baseDir, name.replaceAll("\\.", "/") + ".class"));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int len;
            final byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            byte[] classBinary = baos.toByteArray();
            Class c = defineClass(name, classBinary);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        } catch (IOException e) {
            return null;
        }
    }

    public Class<?> loadClassWithoutParent(String name, boolean resolve) {
        Class c = findLoadedClass(name);
        if (c != null && this.equals(c.getClassLoader())) {
            return c;
        }

        File f = new File(baseDir, name.replaceAll("\\.", "/") + ".class");
        if (f.isFile()) {
            c = defineClass(name, resolve);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c != null) return c;

        File f = new File(baseDir, name.replace('.', '/') + ".class");
        if (f.isFile()) {
            c = defineClass(name, resolve);
            if (c != null) return c;
        }
        return ((EnkanLoader) getParent()).loadClassSiblingFirst(name, resolve, getURLs()[0]);
    }

    @Override
    protected void addURL(URL url) {
        super.addURL(url);
    }
}
