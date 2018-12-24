package enkan.system.devel.compiler;

import enkan.Env;
import enkan.system.ReplResponse;
import enkan.system.Transport;
import enkan.system.devel.CompileResult;
import enkan.system.devel.Compiler;
import org.apache.maven.shared.invoker.*;
import org.apache.maven.shared.utils.cli.CommandLineException;

import java.io.File;
import java.util.Collections;

/**
 * @author kawasima
 */
public class MavenCompiler implements Compiler {
    private String projectDirectory = ".";

    @Override
    public CompileResult execute(Transport t) {
        final InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("pom.xml"));
        request.setGoals(Collections.singletonList("compile"));

        final Invoker invoker = new DefaultInvoker();
        final File mavenHome = new File(Env.getString("MAVEN_HOME",
                Env.getString("M2_HOME", "/opt/maven")));
        if (!mavenHome.exists()) {
            throw new IllegalStateException("MAVEN_HOME not set");
        }
        invoker.setWorkingDirectory(new File(projectDirectory));
        invoker.setMavenHome(mavenHome);
        invoker.setOutputHandler(line -> t.send(ReplResponse.withOut(line)));
        invoker.setErrorHandler(line -> t.send(ReplResponse.withErr(line)));

        CompileResult result = new CompileResult();
        try {
            InvocationResult invocationResult = invoker.execute(request);
            CommandLineException clEx = invocationResult.getExecutionException();
            result.setExecutionException(clEx);
        } catch (MavenInvocationException ex) {
            result.setExecutionException(ex);
        }
        return result;
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }
}
