package enkan.component.doma2;

import org.seasar.doma.jdbc.Config;

public class DomaPrividerUtils {
    private DomaPrividerUtils() {

    }

    public static Config getDefaultConfig(DomaProvider provider) {
        return provider.getDefaultConfig();
    }
}
