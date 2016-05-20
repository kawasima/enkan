package enkan.system.devel;

import enkan.Env;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Collections;

/**
 * @author kawasima
 */
public class MavenCompiler {
    public InvocationResult execute() throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("pom.xml"));
        request.setGoals(Collections.singletonList("compile"));

        Invoker invoker = new DefaultInvoker();
        File mavenHome = new File(Env.getString("MAVEN_HOME",
                Env.getString("M2_HOME", "/opt/maven")));
        if (!mavenHome.exists()) {
            throw new IllegalStateException("MAVEN_HOME not set");
        }
        invoker.setMavenHome(mavenHome);
        return invoker.execute(request);
    }
}
