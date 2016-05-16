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
        invoker.setMavenHome(new File(Env.getString("M2_HOME", "/opt/maven")));
        return invoker.execute(request);
    }
}
