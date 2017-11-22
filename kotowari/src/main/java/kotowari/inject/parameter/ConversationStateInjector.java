package kotowari.inject.parameter;

import enkan.data.ConversationState;
import enkan.data.HttpRequest;
import kotowari.inject.ParameterInjector;

public class ConversationStateInjector implements ParameterInjector<ConversationState> {
    @Override
    public String getName() {
        return "ConversationState";
    }

    @Override
    public boolean isApplicable(Class<?> type, HttpRequest request) {
        return ConversationState.class.isAssignableFrom(type);
    }

    @Override
    public ConversationState getInjectObject(HttpRequest request) {
        ConversationState state = request.getConversationState();
        if (state == null) {
            state = new ConversationState();
            request.setConversationState(state);
        }
        return state;
    }
}
