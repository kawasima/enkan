<#import "../layout/defaultLayout.ftl" as layout>
<@layout.layout "Conversation 2">
  <h1>Conversation page2</h1>
  <p>ConversationState['random'] = ${conversationState.random}</p>
  <form method="post" action="${urlFor('page3')}">
    <input type="hidden" name="__conversation-token" value="${conversationToken!''}"/>
    <button>Next page</button>
  </form>
</@layout.layout>
