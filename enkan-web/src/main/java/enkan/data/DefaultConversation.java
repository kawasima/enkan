package enkan.data;

import javax.enterprise.context.Conversation;
import java.util.Objects;
import java.util.UUID;

/**
 * @author kawasima
 */
public class DefaultConversation implements Conversation {
    private String id;
    private boolean isTransient = true;
    private long timeout = -1;

    /**
     * Create a transient conversation.
     */
    public DefaultConversation() {

    }

    /**
     * Create a long-transactional conversation.
     *
     * @param id the identifier of a conversation
     */
    public DefaultConversation(String id) {
        begin(id);
    }

    @Override
    public void begin() {
        begin(UUID.randomUUID().toString());
    }

    @Override
    public void begin(String id) {
        this.id = id;
        isTransient = false;
    }

    @Override
    public void end() {
        isTransient = true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getTimeout() {
        return -1;
    }

    @Override
    public void setTimeout(long milliseconds) {
        timeout = milliseconds;
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object another) {
        return another != null && Conversation.class.isInstance(another) && Objects.equals(getId(), Conversation.class.cast(another).getId());
    }
}
