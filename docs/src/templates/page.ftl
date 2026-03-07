<#import "layout.ftl" as layout>
<@layout.myLayout>
  <div class="container">
  <div class="row">
    <aside class="col-lg-2">
    <#include "menu.ftl">
    </aside>
    <section class="col-lg-10">
      <div class="row">
        <div class="col-12">
        ${content.body}
        </div>
      </div>
    </section>
  </div>
  </div>
</@layout.myLayout>
