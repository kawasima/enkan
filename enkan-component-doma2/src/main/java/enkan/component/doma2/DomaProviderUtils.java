package enkan.component.doma2;

import org.seasar.doma.jdbc.Config;

/**
 * Utility class for DomaProvider related operations.
 */
public class DomaProviderUtils {
    /**
     * Private constructor to prevent instantiation.
     */
    private DomaProviderUtils() {
        // Prevent instantiation
    }

    /**
     * Retrieves the default configuration from the given DomaProvider.
     *
     * @param provider the DomaProvider instance
     * @return the default Config instance from the provider
     */
    public static Config getDefaultConfig(DomaProvider provider) {
        return provider.getDefaultConfig();
    }
}