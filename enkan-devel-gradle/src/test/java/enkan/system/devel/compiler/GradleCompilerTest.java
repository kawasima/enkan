package enkan.system.devel.compiler;

import enkan.system.ReplResponse;
import enkan.system.Transport;
import enkan.system.devel.CompileResult;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private GradleCompiler newCompiler() {
        GradleCompiler compiler = new GradleCompiler();
        compiler.setProjectDirectory("target/proj");
        compiler.setGradleVersion("8.14.4"); // test project has no wrapper
        return compiler;
    }

    @Test
    public void success() throws IOException {
        FileUtils.copyFileToDirectory(
                new File("src/test/resources/Hello.java"),
                new File("target/proj/src/main/java")
        );
        GradleCompiler compiler = newCompiler();

        List<ReplResponse> responses = new ArrayList<>();
        Transport t = new Transport() {
            @Override
            public void send(ReplResponse response) {
                responses.add(response);
            }

            @Override
            public String recv(long timeout) {
                return null;
            }
        };

        CompileResult result = compiler.execute(t);
        assertThat(result.getExecutionException()).isNull();
    }

    @Test
    public void compileError() throws IOException {
        FileUtils.copyFileToDirectory(
                new File("src/test/resources/HelloError.java"),
                new File("target/proj/src/main/java")
        );
        GradleCompiler compiler = newCompiler();

        List<ReplResponse> responses = new ArrayList<>();
        Transport t = new Transport() {
            @Override
            public void send(ReplResponse response) {
                responses.add(response);
            }

            @Override
            public String recv(long timeout) {
                return null;
            }
        };

        CompileResult result = compiler.execute(t);
        assertThat(result.getExecutionException()).isNotNull();
    }
}
