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
     <div class="form-group">
       <label for="email">Email address</label>
       <input id="email" class="form-control" type="text" name="email" value=""/>
     </div>
     <div>
       <label>Gender</label>
       <label>
         <input id="gender-M" type="radio" name="gender" value="M"/>Male
       </label>
       <label>
         <input id="gender-F" type="radio" name="gender" value="F"/>Female
       </label>
     </div>
     <div class="form-group">
       <label for="birthday">Birthday</label>
       <input id="birthday" class="form-control" type="date" name="birthday" value=""/>
     </div>
     <button type="submit" class="btn btn-primary">Register</button>
   </form>
</@layout.layout>
