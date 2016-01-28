package enkan.exception;

/**
 * @author kawasima
 */
public class FalteringEnvironmentException extends UnrecoverableException {
    private FalteringEnvironmentException(Throwable cause) {
        super("", cause);
    }

    public static FalteringEnvironmentException create(Throwable cause) {
        return new FalteringEnvironmentException(cause);
    }
}
