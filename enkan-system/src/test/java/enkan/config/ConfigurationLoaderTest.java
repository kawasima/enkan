package enkan.config;

import org.junit.jupiter.api.Test;

public class ConfigurationLoaderTest {
    @Test
    public void test() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        Class<?> test1MiddlewareClass = Class.forName("enkan.component.Test1Middleware", false, cl);
        System.out.println(test1MiddlewareClass.hashCode());
        ConfigurationLoader configurationLoader = new ConfigurationLoader(cl);
        System.out.println(Class.forName("enkan.component.Test1Middleware", false, configurationLoader).getClassLoader());
    }
}
