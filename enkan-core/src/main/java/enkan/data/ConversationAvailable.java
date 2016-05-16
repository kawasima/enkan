package enkan.data;

import javax.enterprise.context.Conversation;

/**
 * Conversation
 *
 * @author kawasima
 */
public interface ConversationAvailable extends Extendable {
    default Conversation getConversation() {
        return (Conversation) getExtension("conversation");
    }

    default void setConversation(Conversation conversation) {
        setExtension("conversation", conversation);
    }

    default ConversationState getConversationState() {
        return (ConversationState) getExtension("conversationState");
    }

    default void setConversationState(ConversationState conversationState) {
        setExtension("conversationState", conversationState);
    }

}
