package enkan.system.devel;

import enkan.Env;
import enkan.system.ReplResponse;
import enkan.system.Transport;
import enkan.system.devel.compiler.MavenCompiler;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author kawasima
 */
public class MavenCompilerTest {

    @BeforeEach
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

        assumeTrue(() -> {
            final File mavenHome = new File(Env.getString("MAVEN_HOME",
                    Env.getString("M2_HOME", "/opt/maven")));
            return mavenHome.exists();
        });
        CompileResult result = compiler.execute(t);
        assertThat(result.getExecutionException()).isNull();
    }
}
