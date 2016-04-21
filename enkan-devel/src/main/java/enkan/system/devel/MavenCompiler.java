package enkan.system.devel;

import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Collections;

/**
 * @author kawasima
 */
public class MavenCompiler {
    public void execute() throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("pom.xml"));
        request.setGoals(Collections.singletonList("compile"));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File("/opt/maven"));
        invoker.execute(request);
        invoker.execute(request);
    }
}
