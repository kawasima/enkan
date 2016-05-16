<#import "../layout/defaultLayout.ftl" as layout>
<@layout.layout "Conversation 1">
   <h1>Conversation page1</h1>
   <p>random = ${random}</p>
   <form method="post" action="${urlFor('page2')}">
     <input type="hidden" name="__conversation-token" value="${conversationToken!''}"/>
     <button>Next page</button>
   </form>
</@layout.layout>
