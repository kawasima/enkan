package kotowari.util;

import kotowari.inject.ParameterInjector;
import kotowari.inject.parameter.*;

import java.util.Arrays;
import java.util.LinkedList;

public class ParameterUtils {
    private static final LinkedList<ParameterInjector<?>> defaultParameterInjectors = new LinkedList<>();
    static {
        defaultParameterInjectors.addAll(Arrays.asList(
                new HttpRequestInjector(),
                new ParametersInjector(),
                new SessionInjector(),
                new FlashInjector<>(),
                new PrincipalInjector(),
                new ConversationInjector(),
                new ConversationStateInjector(),
                new LocaleInjector()));
    }

    public static LinkedList<ParameterInjector<?>> getDefaultParameterInjectors() {
        return defaultParameterInjectors;
    }
}
