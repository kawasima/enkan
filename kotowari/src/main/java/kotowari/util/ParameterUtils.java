package kotowari.util;

import enkan.data.ConversationState;
import enkan.data.Flash;
import enkan.data.HttpRequest;
import enkan.data.Session;
import enkan.security.UserPrincipal;

import javax.enterprise.context.Conversation;
import java.util.Map;

public class ParameterUtils {
    public static boolean isReservedType(Class<?> type) {
        return HttpRequest.class.isAssignableFrom(type)
                || Session.class.isAssignableFrom(type)
                || Flash.class.isAssignableFrom(type)
                || Conversation.class.isAssignableFrom(type)
                || ConversationState.class.isAssignableFrom(type)
                || UserPrincipal.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type);
    }
}
