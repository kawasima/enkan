package enkan.exception;

/**
 * @author kawasima
 */
public class UnrecoverableException extends RuntimeException {
    private UnrecoverableException(Throwable cause) {
        super(cause);
    }
    private UnrecoverableException(String msg) {
        super(msg);
    }

    public static UnrecoverableException raise(Throwable cause) {
        throw new UnrecoverableException(cause);
    }

    public static UnrecoverableException create(Throwable cause) {
        return new UnrecoverableException(cause);
    }
    public static UnrecoverableException create(String msg) {
        return new UnrecoverableException(msg);
    }
}
