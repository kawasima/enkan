package enkan.data;

import jakarta.enterprise.context.Conversation;

/**
 * Conversation
 *
 * @author kawasima
 */
public interface ConversationAvailable extends Extendable {
    default Conversation getConversation() {
        return getExtension("conversation");
    }

    default void setConversation(Conversation conversation) {
        setExtension("conversation", conversation);
    }

    default ConversationState getConversationState() {
        return getExtension("conversationState");
    }

    default void setConversationState(ConversationState conversationState) {
        setExtension("conversationState", conversationState);
    }

}
