package enkan.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    public void testBasicLoading() {
        ClassLoader cl = getClass().getClassLoader();
        ConfigurationLoader configurationLoader = new ConfigurationLoader(cl);

        // Verify the loader was created successfully
        assertNotNull(configurationLoader);
    }

    @Test
    public void testReloadableFiles() {
        ClassLoader cl = getClass().getClassLoader();
        ConfigurationLoader configurationLoader = new ConfigurationLoader(cl);

        // Check that reloadableFiles returns a list
        List<File> reloadableFiles = configurationLoader.reloadableFiles();
        assertNotNull(reloadableFiles);
    }

    @Test
    public void testIsTarget() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        ConfigurationLoader configurationLoader = new ConfigurationLoader(cl);

        // Access the protected method using reflection
        java.lang.reflect.Method isTargetMethod = ConfigurationLoader.class.getDeclaredMethod("isTarget", String.class);
        isTargetMethod.setAccessible(true);

        // Test with an existing class
        isTargetMethod.invoke(configurationLoader, "enkan.component.Test1Middleware");
    }

    @Test
    public void testWithCustomDirectory() throws Exception {
        // Create a temporary directory structure with META-INF/reload.xml
        Path classesDir = tempDir.resolve("classes");
        Path metaInfDir = classesDir.resolve("META-INF");
        Files.createDirectories(metaInfDir);
        Files.writeString(metaInfDir.resolve("reload.xml"), "<reload/>");

        // Create a test class file
        Path packageDir = classesDir.resolve("testpackage");
        Files.createDirectories(packageDir);
        Files.writeString(packageDir.resolve("TestClass.class"), "dummy class content");

        // Create a URLClassLoader with our temp directory
        URL[] urls = new URL[]{classesDir.toFile().toURI().toURL()};
        URLClassLoader urlClassLoader = new URLClassLoader(urls, getClass().getClassLoader());

        // Create the ConfigurationLoader using our custom class loader
        ConfigurationLoader configurationLoader = new ConfigurationLoader(urlClassLoader);

        // Check if our directory is recognized as reloadable
        List<File> reloadableFiles = configurationLoader.reloadableFiles();
        assertTrue(reloadableFiles.stream().anyMatch(file ->
                file.getAbsolutePath().equals(classesDir.toFile().getAbsolutePath())));

        // Check if our test class is considered a target
        java.lang.reflect.Method isTargetMethod = ConfigurationLoader.class.getDeclaredMethod("isTarget", String.class);
        isTargetMethod.setAccessible(true);
        Boolean result = (Boolean) isTargetMethod.invoke(configurationLoader, "testpackage.TestClass");
        assertTrue(result);
    }

    @Test
    public void testClassLoading() {
        ClassLoader cl = getClass().getClassLoader();
        ConfigurationLoader configurationLoader = new ConfigurationLoader(cl);

        // Try to load an existing class through the loader
        try {
            Class<?> loadedClass = configurationLoader.loadClass("enkan.config.ConfigurationLoader");
            assertNotNull(loadedClass);
            assertEquals("enkan.config.ConfigurationLoader", loadedClass.getName());
        } catch (ClassNotFoundException e) {
            fail("Should be able to load existing class: " + e.getMessage());
        }
    }
}