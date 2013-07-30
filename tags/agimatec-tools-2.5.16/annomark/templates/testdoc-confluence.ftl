h1. Test documentation

<#list classes?sort as class>
h2. ${class.name} (${class.getTestAnnotation().getStringValue("type")})
<#if class.comment??>${class.comment}</#if>
<#list class.getAnnotatedMethods() as method>

h3. ${method.name}
<#if method.comment??>${method.comment}</#if>
   
</#list>
</#list>    

