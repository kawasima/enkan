package enkan.system.devel;

import java.io.OutputStream;
import java.io.Serializable;

/**
 * A result of compiling the project.
 *
 * @author kawasima
 */
public class CompileResult implements Serializable {
    private Throwable executionException;
    private OutputStream out;
    private OutputStream err;

    public CompileResult() {

    }

    public Throwable getExecutionException() {
        return executionException;
    }

    public void setExecutionException(Throwable executionException) {
        this.executionException = executionException;
    }

    public OutputStream getOut() {
        return out;
    }

    public OutputStream getErr() {
        return err;
    }
}
