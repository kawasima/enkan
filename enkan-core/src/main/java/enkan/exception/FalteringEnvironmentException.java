package enkan.exception;

/**
 * If the request is retried, it may be success.
 *
 * @author kawasima
 */
public class FalteringEnvironmentException extends UnrecoverableException {
    public FalteringEnvironmentException() {
        super("May be success if retry");
    }
    public FalteringEnvironmentException(Throwable cause) {
        super("May be success if retry", cause);
    }
}
