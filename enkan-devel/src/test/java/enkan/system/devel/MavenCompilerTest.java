package enkan.system.devel;

import enkan.system.ReplResponse;
import enkan.system.Transport;
import enkan.system.devel.compiler.MavenCompiler;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author kawasima
 */
public class MavenCompilerTest {

    @Before
    public void setup() throws IOException {
        FileUtils.deleteDirectory(new File("target/proj"));
        FileUtils.forceMkdir(new File("target/proj/src/main/java"));

        FileUtils.copyFileToDirectory(
                new File("src/test/resources/pom.xml"),
                new File("target/proj")
        );
    }


    @Test
    public void success() throws IOException {
        FileUtils.copyFileToDirectory(
                new File("src/test/resources/Hello.java"),
                new File("target/proj/src/main/java")
        );

        MavenCompiler compiler = new MavenCompiler();
        compiler.setProjectDirectory("target/proj");

        Transport t = new Transport() {
            @Override
            public void send(ReplResponse response) {
            }

            @Override
            public String recv(long timeout) {
                return null;
            }
        };

        CompileResult result = compiler.execute(t);
        Assert.assertNull(result.getExecutionException());
    }
}
