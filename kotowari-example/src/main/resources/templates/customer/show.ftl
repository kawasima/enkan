<#import "../layout/defaultLayout.ftl" as layout>
<@layout.layout "Detail of customer">
  <h1>Detail of customer</h1>
  <dl class="dl-horizontal">
    <dt>Name</dt>
    <dd>${customer.name}</dd>

    <dt>Email address</dt>
    <dd>${customer.email}</dd>

    <dt>Gender</dt>
    <dd>
      <#switch (customer.gender)!>
        <#case "F">Female<#break>
        <#case "M">Male<#break>
      </#switch>
    </dd>

    <dt>Birthday</dt>
    <dd>${(customer.birthday)!}</dd>
  </dl>
  <a href="${urlFor('edit?id=' + customer.id)}">Edit</a>
  <a href="${urlFor('delete?id=' + customer.id + '&_method=delete')}">Remove</a>
</@layout.layout>
