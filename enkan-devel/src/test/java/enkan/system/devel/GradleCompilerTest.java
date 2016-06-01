package enkan.system.devel;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;

import enkan.Env;
import net.unit8.moshas.helper.StringUtil;

/**
 * simple instance making test.
 * @author Toast kid
 *
 */
public class GradleCompilerTest {

    /**
     * If your env has "GRADLE_HOME" or "gradle.home", this test method attempt to make instance.
     * @throws MavenInvocationException
     */
    @Test
    public void makableInstance() throws MavenInvocationException {
        if (StringUtil.isBlank(Env.getString("GRADLE_HOME", Env.getString("gradle.home", null)))) {
            return;
        }
        new GradleCompiler().execute();
    }
}
