<#import "layout.ftl" as layout>
<@layout.myLayout>
  <div class="container">
  <div class="row">
    <section class="col-lg-3">
    <#include "menu.ftl">
    </section>
    <section class="col-lg-9">
      <div class="container">
        <div class="row">
          <div class="col-lg-12">
          ${content.body}
          </div>
        </div>
      </div>
    </section>
  </div>
  </div>
</@layout.myLayout>
