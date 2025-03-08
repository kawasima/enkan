package enkan.system.repl;

import enkan.config.EnkanSystemFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class ConfigurationLoader extends URLClassLoader {
    private final File classesDirectory;

    public ConfigurationLoader(File classesDirectory) throws Exception {
        super(new URL[]{classesDirectory.toURI().toURL()},
              Thread.currentThread().getContextClassLoader());
        this.classesDirectory = classesDirectory;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (isTarget(name)) {
            File classFile = getClassFile(name);
            if (classFile != null && classFile.exists()) {
                Class<?> c = findClass(name);
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }
        return super.loadClass(name, resolve);
    }

    private boolean isTarget(String className) {
        File classFile = getClassFile(className);
        return classFile != null && classFile.exists();
    }

    public Class<? extends EnkanSystemFactory> loadConfiguration(String className) throws Exception {
        File classFile = getClassFile(className);
        if (classFile == null || !classFile.exists()) {
            throw new IllegalArgumentException("Class file not found: " + className);
        }

        Class<?> clazz = loadClass(className);
        if (!EnkanSystemFactory.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(className + " is not an EnkanSystemFactory");
        }

        @SuppressWarnings("unchecked")
        Class<? extends EnkanSystemFactory> factoryClass = (Class<? extends EnkanSystemFactory>) clazz;
        return factoryClass;
    }

    private File getClassFile(String className) {
        String classPath = className.replace('.', '/') + ".class";
        return new File(classesDirectory, classPath);
    }
} 