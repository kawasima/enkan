package kotowari.util;

import enkan.data.ConversationState;
import enkan.data.Flash;
import enkan.data.HttpRequest;
import enkan.data.Session;
import enkan.security.UserPrincipal;
import kotowari.inject.ParameterInjector;
import kotowari.inject.parameter.*;

import javax.enterprise.context.Conversation;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
