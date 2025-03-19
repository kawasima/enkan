package enkan.exception;

/**
 * This exception is thrown when the exception is caused by a framework bug.
 *
 * @author kawasima
 */
public final class UnreachableException extends UnrecoverableException {
    public UnreachableException() {
        this(null);
    }

    public UnreachableException(Throwable cause) {
        super("This exception has proved a framework bug.", cause);
    }
}
