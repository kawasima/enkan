package enkan.exception;

/**
 * @author kawasima
 */
public abstract class UnrecoverableException extends RuntimeException {
    protected UnrecoverableException(String message, Throwable cause) {
        super(message, cause);
    }

    protected UnrecoverableException(String msg) {
        super(msg);
    }
}
