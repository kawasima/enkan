package enkan.exception;

/**
 * Thrown when the application encounters a transient infrastructure failure
 * from which recovery is possible by retrying.
 *
 * <p>Typical causes include temporary I/O errors, momentary resource
 * exhaustion, or short-lived network interruptions.  Unlike
 * {@link MisconfigurationException}, this exception does <em>not</em>
 * require developer intervention — simply retrying the same operation may
 * succeed.
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
