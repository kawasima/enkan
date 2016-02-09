<#import "../layout/defaultLayout.ftl" as layout>
<@layout.layout "Detail of customer">
  <h1>Detail of customer</h1>
  <table class="table">
    <tbody>
      <tr>
        <th>Name</th>
        <td>${customer.name}</td>
      </tr>
    </tbody>
  </table>
  <a href="${urlFor('edit?id=' + customer.id)}">Edit</a>
  <a href="${urlFor('delete?id=' + customer.id + '&_method=delete')}">Remove</a>
</@layout.layout>
