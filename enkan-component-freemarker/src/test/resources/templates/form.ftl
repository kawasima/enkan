<#if form.hasErrors()>INVALID<#else>VALID</#if>
<#if form.hasErrors("name")>name: ${form.getErrors("name")?join(", ")}</#if>