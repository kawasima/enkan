<#import "../layout/defaultLayout.ftl" as layout>
<@layout.layout "Edit customer">
  <h1>Edit customer</h1>
  <form method="post" action="${urlFor("update?id=" + id)}">
    <input type="hidden" name="_method" value="PUT"/>
    <div class="form-group<#if customer.hasErrors("name")> has-error</#if>">
      <label for="name">Account</label>
      <input id="name" class="form-control" type="text" name="name" value="${customer.name!''}"/>
      <span class="help-block"><#if customer.hasErrors("name")>${customer.getErrors("name")?join(",")}</#if></span>
    </div>
    <div class="form-group">
      <label for="password">Password</label>
      <input id="password" class="form-control" type="password" name="password" value=""/>
    </div>
    <button type="submit" class="btn btn-primary">Update</button>
   </form>
</@layout.layout>
