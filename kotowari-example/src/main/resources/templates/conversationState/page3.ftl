<#import "../layout/defaultLayout.ftl" as layout>
<@layout.layout "Conversation 3">
   <h1>Conversation page3</h1>
   <p>ConversationState['random'] = ${conversationState.random}</p>
   <a href="${urlFor('page1')}">Conversation Top</a>
</@layout.layout>
