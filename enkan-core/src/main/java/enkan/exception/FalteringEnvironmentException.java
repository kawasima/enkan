package enkan.exception;

/**
 * If the request is retried, it may be success.
 *
 * @author kawasima
 */
public class FalteringEnvironmentException extends UnrecoverableException {
    public FalteringEnvironmentException(Throwable cause) {
        super("Retry. May be success.", cause);
    }
}
