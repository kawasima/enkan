package enkan.config;

import enkan.exception.UnreachableException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class ConfigurationLoader extends ClassLoader {
    List<File> dirs;

    public ConfigurationLoader(ClassLoader parent) {
        super(parent);
        URL[] urls = ((URLClassLoader) parent).getURLs();

        dirs = Arrays.stream(urls)
                .filter(this::isDirectory)
                .filter(d -> {
                    try {
                        return new URLClassLoader(new URL[]{ d }, null).getResource("META-INF/reload.xml") != null;
                    } catch (NoClassDefFoundError e) {
                        return false;
                    }
                })
                .map(url -> {
                    try {
                        return new File(url.toURI());
                    } catch(URISyntaxException e) {
                        throw UnreachableException.create(e);
                    }
                })
                .collect(Collectors.toList());
    }

    protected boolean contains(File dir, String path) {
        return Files.exists(dir.toPath().resolve(path.replace('.', '/') + ".class"));
    }

    protected boolean isDirectory(URL url) {
        try {
            return url.getProtocol().equals("file") && new File(url.toURI()).isDirectory();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    protected boolean isTarget(String name) {
        return dirs.stream()
                .anyMatch(d -> contains(d, name));
    }

    private void definePackage(String name) {
        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            String pkgName = name.substring(0, idx);
            Package pkg = getPackage(pkgName);
            if (pkg == null) {
                definePackage(pkgName, null, null, null, null, null, null, null);
            }
        }
    }

    private Class defineClass(String name, boolean resolve) {
        try (InputStream in = getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (in == null) return null;
            int len;
            final byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            byte[] classBinary = baos.toByteArray();

            Class<?> c = defineClass(name, classBinary, 0, classBinary.length);
            definePackage(name);
            if (resolve)
                resolveClass(c);
            return c;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c != null) return c;

        try {
            Method findLoadedClassMethod = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            findLoadedClassMethod.setAccessible(true);
            c = (Class) findLoadedClassMethod.invoke(getParent(), name);
            if (c != null){
                return c;
            }
        } catch (Exception e) {
            throw new ClassNotFoundException(name, e);
        }


        if (isTarget(name)) {
            c = defineClass(name, resolve);
            if (c != null)
                return c;
        }

        return super.loadClass(name, resolve);
    }

    public List<File> reloadableFiles() {
        return dirs;
    }
}
