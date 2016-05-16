package enkan.exception;

import enkan.util.MergeableResourceBundleControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * If the exception is caused by a misconfiguration, throws this exception.
 *
 * MisconfigurationException is for the developers.
 *
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

    public MisconfigurationException(String code, Object... arguments) {
        super(code, Arrays.stream(arguments)
                .filter(arg -> arg instanceof Throwable)
                .map(arg -> (Throwable) arg)
                .findFirst().orElse(null));
        String problemFmt = misconfigurationMessages.getString(code + ".problem");
        problem = String.format(Locale.US, problemFmt, arguments);

        String solutionFmt = misconfigurationMessages.getString(code + ".solution");
        solution = String.format(Locale.US, solutionFmt, arguments);

    }

    public String getCode() {
        return super.getMessage();
    }

    public String getProblem() {
        return problem;
    }

    public String getSolution() {
        return solution;
    }
}
