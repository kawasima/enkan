<div class="form-group<#if $entity$.hasErrors("$field$")> has-error</#if>">
  <label for="$entity$-$field$">$Field$</label>
  <input id="$entity$-$field$" class="form-control" type="text" name="$field$" value="${$entity$.$field$!''}"/>
  <span class="help-block"><#if $entity$.hasErrors("$field$")>${$entity$.getErrors("$field$")?join(",")}</#if></span>
</div>
