package enkan.exception;

/**
 * We think unreachable Unreachable
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
