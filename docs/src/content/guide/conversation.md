type=page
status=published
title=Conversations | Enkan
~~~~~~

# Conversations

Enkan supports the pseudo Java EE Conversation scope.

## Conversation

`Conversation` gives an unique id to the series of long transactional requests.

If you start a conversation, describe as follows.

```language-java
HttpResponse startConversation(Conversation conversation) {
    if (conversation.isTransient()) conversation.begin();
}
```

And if you end a conversation, describe as follows. 

```language-java
HttpResponse endConversation(Conversation conversation) {
    if (!conversation.isTransient()) conversation.end();
    
    // Transaction code...
}
```

A conversation send to the server via HTTP parameters. So, you need to write the following code in each form. 

```language-html
<input type="hidden" name="__converstation-id" value="${conversationId}"/>
```

## Conversation state

A `ConversationState` can have pairs of key and value related with conversation.
If conversation ends, conversation state will be deleted. 

```language-java
HttpResponse conversation(Conversation conversation) {
    if (conversation.isTransient()) conversation.begin();
    
    ConversationState conversationState = new ConversationState();
    conversationState.put("random", randomValue);
    
    return builder(templateEngine.render("template"))
            .set(HttpResponse::setConversationState, conversationState)
            .build();
}
```

In above code, conversation state will be stored to the KVS store in the same case of Session.
