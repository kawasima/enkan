package enkan.system.devel;

import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Compiler interface.
 * @author Toast kid
 *
 */
public interface Compiler {
    public InvocationResult execute() throws MavenInvocationException;
}
