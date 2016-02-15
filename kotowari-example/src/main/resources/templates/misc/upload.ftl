<#import "../layout/defaultLayout.ftl" as layout>
<@layout.layout "Upload">
  <h1>File upload</h1>
  <form action="upload" method="post" enctype="multipart/form-data">
    <input type="file" name="datafile"/>
    <input type="text" name="description"/>
    <button type="submit">Send</button>
  </form>
</@layout.layout>
