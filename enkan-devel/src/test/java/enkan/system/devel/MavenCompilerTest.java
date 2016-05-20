package enkan.system.devel;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;

/**
 * @author kawasima
 */
public class MavenCompilerTest {
    @Test
    public void test() throws MavenInvocationException {
        new MavenCompiler().execute();
    }
}
