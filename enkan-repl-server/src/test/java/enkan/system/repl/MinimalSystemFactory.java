package enkan.system.repl;

import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;

/**
 * A minimal {@link EnkanSystemFactory} with no components, used in tests to
 * keep JShellRepl startup time as short as possible.
 */
public class MinimalSystemFactory implements EnkanSystemFactory {
    @Override
    public EnkanSystem create() {
        return EnkanSystem.of();
    }
}
