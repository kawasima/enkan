<#import "../layout/defaultLayout.ftl" as layout>
<@layout.layout "List of customer">
  <h1>List of customers</h1>

  <#list customers>
  <table>
    <thead>
      <tr>
        <th>Name</th>
      </tr>
    </thead>
    <tbody>
      <#items as customer>
        <tr>
          <td><a href="/customer/${customer.id}">${customer.name}</a></td>
        </tr>
      </#items>
    </tbody>
  </table>
  <#else>
  <div class="alert alert-info" role="alert">
     <p>No customers</p>
  </div>
  </#list>

  <a href="${urlFor('newForm')}">New register</a>
</@layout.layout>
