package enkan.exception;

import enkan.util.MergeableResourceBundleControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.ResourceBundle;

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

    protected MisconfigurationException(String code, Object... arguments) {
        super(code);
        String problemFmt = misconfigurationMessages.getString(code + ".problem");
        problem = MessageFormatter.arrayFormat(problemFmt, arguments).getMessage();

        String solutionFmt = misconfigurationMessages.getString(code + ".solution");
        solution = MessageFormatter.arrayFormat(solutionFmt, arguments).getMessage();
    }

    public static MisconfigurationException create(String code, Object... arguments){
        return new MisconfigurationException(code, arguments);
    }

    public String getProblem() {
        return problem;
    }

    public String getSolution() {
        return solution;
    }
}
