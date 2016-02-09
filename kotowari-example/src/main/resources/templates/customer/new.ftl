<#import "../layout/defaultLayout.ftl" as layout>
<@layout.layout "New customer">
   <h1>New customer</h1>
   <form method="post" action="${urlFor('create')}">
     <div class="form-group">
       <label for="name">Account</label>
       <input id="name" class="form-control" type="text" name="name" value="${customer.name!''}"/><#if customer.hasErrors("name")!false>${customer.getErrors("name")?join(",")}</#if>
     </div>
     <div class="form-group">
       <label for="password">Password</label>
       <input id="password" class="form-control" type="password" name="password" value=""/>
     </div>
     <button type="submit" class="btn btn-primary">Register</button>
   </form>
</@layout.layout>
