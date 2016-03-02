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
    <div class="form-group<#if customer.hasErrors("password")> has-error</#if>">
      <label for="password">Password</label>
      <input id="password" class="form-control" type="password" name="password" value=""/>
      <span class="help-block"><#if customer.hasErrors("password")>${customer.getErrors("password")?join(",")}</#if></span>
    </div>
     <div class="form-group<#if customer.hasErrors("email")> has-error</#if>">
       <label for="email">Email address</label>
       <input id="email" class="form-control" type="text" name="email" value="${customer.email}"/>
       <span class="help-block"><#if customer.hasErrors("email")>${customer.getErrors("email")?join(",")}</#if></span>
     </div>
     <div>
       <label>Gender</label>
       <label>
         <input id="gender-M" type="radio" name="gender" value="M"<#if (customer.gender)! == 'M'> checked</#if>/>Male
       </label>
       <label>
         <input id="gender-F" type="radio" name="gender" value="F"<#if (customer.gender)! == 'F'> checked</#if>/>Female
       </label>
     </div>
     <div class="form-group<#if customer.hasErrors("birthday")> has-error</#if>">
       <label for="birthday">Birthday</label>
       <input id="birthday" class="form-control" type="date" name="birthday" value="${(customer.birthday)!}"/>
     </div>

    <button type="submit" class="btn btn-primary">Update</button>
   </form>
</@layout.layout>
