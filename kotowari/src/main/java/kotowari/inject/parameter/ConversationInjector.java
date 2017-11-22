package kotowari.inject.parameter;

import enkan.data.HttpRequest;
import kotowari.inject.ParameterInjector;

import javax.enterprise.context.Conversation;

public class ConversationInjector implements ParameterInjector<Conversation> {
    @Override
    public String getName() {
        return "Conversation";
    }

    @Override
    public boolean isApplicable(Class<?> type, HttpRequest request) {
        return Conversation.class.isAssignableFrom(type);
    }

    @Override
    public Conversation getInjectObject(HttpRequest request) {
        return request.getConversation();
    }
}
