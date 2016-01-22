package enkan.system.loader;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kawasima
 */
public class EnkanLoader extends URLClassLoader {
    static URL[] EMPTY_URLS = new URL[]{};
    static ConcurrentHashMap<URL, DisposableClassLoader> children = new ConcurrentHashMap<>();
    final List<URL> loaderUrls = new ArrayList<>();
    URLClassLoader parent;

    public EnkanLoader(URLClassLoader parent) {
        super(EMPTY_URLS, parent);

        List<URL> unreloadedUrls = new ArrayList<>();

        for (URL url : parent.getURLs()) {
            if (url.toString().endsWith(".jar") || url.toString().contains("enkan-system")) {
                unreloadedUrls.add(url);
            }
        }

        this.parent = new URLClassLoader(unreloadedUrls.toArray(new URL[unreloadedUrls.size()]), parent);

        Arrays.stream(parent.getURLs())
                .filter(url -> !unreloadedUrls.contains(url))
                .forEach(url -> {
                    DisposableClassLoader ucl = new DisposableClassLoader(url, this.parent);
                    children.put(url, ucl);
                    loaderUrls.add(url);
                });
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c != null) return c;

        for (URL url : loaderUrls) {
            URLClassLoader ucl = children.get(url);
            try {
                return ucl.loadClass(name);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                // ignore
            }
        }
        try {
            return super.loadClass(name, false);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public synchronized void reload() {
        children.clear();
        System.gc();
        for (URL url : loaderUrls) {
            children.put(url, new DisposableClassLoader(url, getParent()));
        }
    }

    static class DisposableClassLoader extends URLClassLoader {
        private File baseDir;

        DisposableClassLoader(URL url, ClassLoader parent) {
            super(new URL[]{ url }, parent);
            try {
                baseDir =new File(url.toURI());
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
                return defineClass(name, classBinary);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class c = findLoadedClass(name);
            if (c != null) return c;

            File f = new File(baseDir, name.replaceAll("\\.", "/") + ".class");
            if (f.isFile()) {
                c = defineClass(name, resolve);
                if (c != null) return c;
            }
            return super.loadClass(name, resolve);
        }

        @Override
        protected void addURL(URL url) {
            super.addURL(url);
        }
    }
}
