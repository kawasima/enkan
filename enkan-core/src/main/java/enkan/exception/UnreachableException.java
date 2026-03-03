package enkan.exception;

/**
 * Thrown when execution reaches a code path that the framework authors
 * considered impossible.
 *
 * <p>Occurrences of this exception always indicate a bug in the framework
 * itself.  They should be reported as issues so that the underlying
 * invariant violation can be identified and fixed.
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
