package enkan.system.devel.compiler;

import enkan.system.ReplResponse;
import enkan.system.Transport;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class GradleCompilerTest {

    @BeforeEach
    public void setup() throws IOException {
        FileUtils.deleteDirectory(new File("target/proj"));
        FileUtils.forceMkdir(new File("target/proj/src/main/java"));

        FileUtils.copyFileToDirectory(
                new File("src/test/resources/build.gradle"),
                new File("target/proj")
        );
    }


    @Test
    public void success() throws IOException {
        FileUtils.copyFileToDirectory(
                new File("src/test/resources/Hello.java"),
                new File("target/proj/src/main/java")
        );
        GradleCompiler compiler = new GradleCompiler();
        compiler.setProjectDirectory("target/proj");
        Transport t = new Transport() {
            @Override
            public void send(ReplResponse response) {
                assertThat(response.getOut().contains("BUILD SUCCESSFUL")).isTrue();
            }

            @Override
            public String recv(long timeout) {
                return null;
            }
        };
        compiler.execute(t);
    }

    @Test
    public void compileError() throws IOException {
        FileUtils.copyFileToDirectory(
                new File("src/test/resources/HelloError.java"),
                new File("target/proj/src/main/java")
        );
        GradleCompiler compiler = new GradleCompiler();
        compiler.setProjectDirectory("target/proj");
        Transport t = new Transport() {
            @Override
            public void send(ReplResponse response) {
                if (response.getStatus().contains(ReplResponse.ResponseStatus.ERROR)) {
                    assertThat(response.getErr().contains("FAILURE")).isTrue();
                } else {
                    assertThat(response.getOut().contains(":compileJava FAILED")).isTrue();
                }
            }

            @Override
            public String recv(long timeout) {
                return null;
            }
        };
        compiler.execute(t);
    }

}
