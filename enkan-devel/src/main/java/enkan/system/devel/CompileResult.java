package enkan.system.devel;

import java.io.Serializable;

/**
 * A result of compiling the project.
 *
 * @author kawasima
 */
public class CompileResult implements Serializable {
    private Throwable executionException;

    public Throwable getExecutionException() {
        return executionException;
    }

    public void setExecutionException(Throwable executionException) {
        this.executionException = executionException;
    }
}
