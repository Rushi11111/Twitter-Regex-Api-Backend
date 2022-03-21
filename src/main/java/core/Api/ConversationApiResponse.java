package core.Api;

import core.Entites.Conversation;

import java.util.List;

public class ConversationApiResponse {
    private List<Conversation> conversations;
    private long pointer;

    public ConversationApiResponse(List<Conversation> conversations) {
        this.conversations = conversations;
        if(conversations.size() >= 1)
            this.pointer = conversations.get(conversations.size()-1).getConversationId();
        else
            this.pointer = 0;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public long getPointer() {
        return pointer;
    }

    public void setPointer(long pointer) {
        this.pointer = pointer;
    }
}
