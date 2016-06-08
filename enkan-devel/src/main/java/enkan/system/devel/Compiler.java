package enkan.system.devel;

import enkan.system.Transport;

/**
 * Compiler interface.
 *
 * @author Toast kid
 */
public interface Compiler {
    CompileResult execute(Transport transport);
}
