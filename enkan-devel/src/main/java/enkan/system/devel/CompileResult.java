package enkan.system.devel;

import java.io.Serializable;

/**
 * A result of compiling the project.
 *
 * @author kawasima
 */
public record CompileResult(Throwable executionException) implements Serializable {
    public static CompileResult success() {
        return new CompileResult(null);
    }

    public static CompileResult failure(Throwable exception) {
        return new CompileResult(exception);
    }
}
