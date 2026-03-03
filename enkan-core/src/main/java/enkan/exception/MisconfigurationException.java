package enkan.exception;

import enkan.util.MergeableResourceBundleControl;

import java.text.MessageFormat;
import java.util.*;

/**
 * If the exception is caused by a misconfiguration, throws this exception.
 * MisconfigurationException is for the developers.
 *
 * @author kawasima
 */
public class MisconfigurationException extends UnrecoverableException {
    private final String problem;
    private final String solution;

    static final ResourceBundle misconfigurationMessages;

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
        problem = new MessageFormat(problemFmt, misconfigurationMessages.getLocale()).format(arguments);
        String solutionFmt = misconfigurationMessages.getString(code + ".solution");
        solution = new MessageFormat(solutionFmt, misconfigurationMessages.getLocale()).format(arguments);

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
