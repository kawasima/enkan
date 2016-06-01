package enkan.system.devel;

import java.io.File;
import java.util.Collections;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import enkan.Env;

public class GradleCompiler implements Compiler {

    @Override
    public InvocationResult execute() throws MavenInvocationException {
        final InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("build.gradle"));
        request.setGoals(Collections.singletonList("compileJava"));

        final Invoker invoker = new GradleInvoker();
        final String pathToGradle = Env.getString("GRADLE_HOME",
                Env.getString("gradle.home", "/bin/gradle"));

        final File gradleHome = new File(pathToGradle);
        if (!gradleHome.exists()) {
            throw new IllegalStateException("GRADLE_HOME not set");
        }
        invoker.setMavenHome(gradleHome);
        return invoker.execute(request);
    }

}
