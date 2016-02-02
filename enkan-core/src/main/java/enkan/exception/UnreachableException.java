package enkan.exception;

/**
 * @author kawasima
 */
public final class UnreachableException extends UnrecoverableException {
    private UnreachableException() {
        this(null);
    }

    private UnreachableException(Throwable cause) {
        super("This exception has proved a framework bug.", cause);
    }

    public static UnreachableException create() {
        return new UnreachableException();
    }

    public static UnreachableException create(Throwable cause) {
        return new UnreachableException(cause);
    }

}
