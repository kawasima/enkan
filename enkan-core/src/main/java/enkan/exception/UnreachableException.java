package enkan.exception;

/**
 * @author kawasima
 */
public final class UnreachableException extends UnrecoverableException {
    private static final UnreachableException WITHOUT_CAUSE_EXCEPTION = new UnreachableException();

    private UnreachableException() {
        this(null);
    }

    private UnreachableException(Throwable cause) {
        super("This exception has proved a framework bug.", cause);
    }

    public static UnreachableException create() {
        return WITHOUT_CAUSE_EXCEPTION;
    }

    public static UnreachableException create(Throwable cause) {
        return new UnreachableException(cause);
    }

}
