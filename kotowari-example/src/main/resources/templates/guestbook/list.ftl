<#import "../layout/defaultLayout.ftl" as layout>
<@layout.layout "Login">
  <form method="post" action="${urlFor('post')}">
     <input type="hidden" name="__conversation-token" value="${conversationToken!''}"/>
     <input type="text" name="message">
     <button class="btn btn-primary" type="submit">Post</button>
  </form>

  <#list guestbooks>
    <ul>
      <#items as guestbook>
        <li>${guestbook.message} (${guestbook.name} ${guestbook.postedAt}) </li>
      </#items>
    </ul>
  <#else>
  <div class="alert alert-info" role="alert">
     <p>No messages</p>
  </div>
  </#list>

</@layout.layout>
