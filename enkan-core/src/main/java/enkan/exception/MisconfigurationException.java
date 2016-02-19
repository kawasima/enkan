package enkan.exception;

import enkan.util.MergeableResourceBundleControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.*;

/**
 * @author kawasima
 */
public class MisconfigurationException extends UnrecoverableException {
    private static Logger LOG = LoggerFactory.getLogger("enkan.misconfiguration");
    private String problem;
    private String solution;

    static ResourceBundle misconfigurationMessages;

    static {
        misconfigurationMessages = ResourceBundle.getBundle("META-INF/misconfiguration", new MergeableResourceBundleControl());
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ":" + problem;
    }

    protected MisconfigurationException(String code, Throwable cause, Object... arguments) {
        super(code, cause);
        String problemFmt = misconfigurationMessages.getString(code + ".problem");
        problem = String.format(Locale.US, problemFmt, arguments);

        String solutionFmt = misconfigurationMessages.getString(code + ".solution");
        solution = String.format(Locale.US, solutionFmt, arguments);
    }

    public static MisconfigurationException create(String code, Object... arguments){
        Optional<Throwable> cause = Arrays.stream(arguments)
                .filter(arg -> arg instanceof Throwable)
                .map(arg -> (Throwable) arg)
                .findFirst();
        return new MisconfigurationException(code, cause.orElse(null), arguments);
    }

    public String getProblem() {
        return problem;
    }

    public String getSolution() {
        return solution;
    }
}
