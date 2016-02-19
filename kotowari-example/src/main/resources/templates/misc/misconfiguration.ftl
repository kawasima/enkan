<#import "../layout/defaultLayout.ftl" as layout>
<@layout.layout "Example">
  <h1>Kotowari examples</h1>
  <ul>
    <li><a href="${urlFor("kotowari.example.controller.MiscController", "uproadForm")}">Upload form</a></li>
  </ul>
</@layout.layout>
