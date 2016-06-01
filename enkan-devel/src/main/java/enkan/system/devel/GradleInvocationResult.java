package enkan.system.devel;

import org.apache.maven.shared.invoker.InvocationResult;
import org.codehaus.plexus.util.cli.CommandLineException;

public class GradleInvocationResult implements InvocationResult {

    /**
     * The exception that prevented to execute the command line, will be
     * <code>null</code> if Maven could be successfully started.
     */
    private CommandLineException executionException;

    /**
     * The exit code reported by the Maven invocation.
     */
    private int exitCode = Integer.MIN_VALUE;

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public CommandLineException getExecutionException() {
        return executionException;
    }

    /**
     * Sets the exit code reported by the Maven invocation.
     *
     * @param exitCode
     *            The exit code reported by the Maven invocation.
     */
    void setExitCode(final int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Sets the exception that prevented to execute the command line.
     *
     * @param executionException
     *            The exception that prevented to execute the command line, may
     *            be <code>null</code>.
     */
    void setExecutionException(final CommandLineException executionException) {
        this.executionException = executionException;
    }

}
