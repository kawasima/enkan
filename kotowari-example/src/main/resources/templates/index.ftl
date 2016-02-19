<#import "layout/defaultLayout.ftl" as layout>
<@layout.layout "Example">
  <h1>Kotowari examples</h1>
  <ul>
    <li><a href="${urlFor("kotowari.example.controller.MiscController", "counter")}">Counter (Using session)</a></li>
    <li><a href="${urlFor("kotowari.example.controller.MiscController", "uploadForm")}">File upload</a></li>
    <li><a href="${urlFor("kotowari.example.controller.CustomerController", "index")}">CRUD</a></li>
  </ul>
  <hr/>
  <ul>
    <li><a href="${urlFor("kotowari.example.controller.HospitalityDemoController", "misconfiguration")}">Misconfiguration demo</a></li>
    <li><a href="${urlFor("kotowari.example.controller.HospitalityDemoController", "unreachable")}">Unreachable Exception demo</a></li>
  </ul>
</@layout.layout>
