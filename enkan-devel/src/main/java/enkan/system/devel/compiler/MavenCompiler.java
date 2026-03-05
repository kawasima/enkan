package enkan.system.devel.compiler;

import enkan.Env;
import enkan.exception.MisconfigurationException;
import enkan.system.ReplResponse;
import enkan.system.Transport;
import enkan.system.devel.CompileResult;
import enkan.system.devel.Compiler;
import org.apache.maven.shared.invoker.*;
import org.apache.maven.shared.utils.cli.CommandLineException;

import java.io.File;

/**
 * @author kawasima
 */
public class MavenCompiler implements Compiler {
    private String projectDirectory = ".";

    @Override
    public CompileResult execute(Transport t) {
        final File mavenHome = new File(Env.getString("MAVEN_HOME",
                Env.getString("M2_HOME", "/opt/maven")));
        if (!mavenHome.exists()) {
            throw new MisconfigurationException("devel.MAVEN_HOME_NOT_SET");
        }

        final InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(projectDirectory, "pom.xml"));
        request.addArg("compile");
        request.setBaseDirectory(new File(projectDirectory));
        request.setMavenHome(mavenHome);
        request.setOutputHandler(line -> t.send(ReplResponse.withOut(line)));
        request.setErrorHandler(line -> t.send(ReplResponse.withErr(line)));

        final Invoker invoker = new DefaultInvoker();

        try {
            InvocationResult invocationResult = invoker.execute(request);
            CommandLineException clEx = invocationResult.getExecutionException();
            if (clEx != null) {
                return CompileResult.failure(clEx);
            } else if (invocationResult.getExitCode() != 0) {
                return CompileResult.failure(new IllegalStateException(
                        "Maven compile failed with exit code " + invocationResult.getExitCode()));
            }
        } catch (MavenInvocationException ex) {
            return CompileResult.failure(ex);
        }
        return CompileResult.success();
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }
}
