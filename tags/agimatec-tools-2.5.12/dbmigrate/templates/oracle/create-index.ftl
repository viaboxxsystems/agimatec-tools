CREATE<#if index.unique> UNIQUE</#if> INDEX ${index.indexName} ON ${index.tableName} (<#list index.columns as col>${col}<#if index.columns?last != col>, </#if></#list>) TABLESPACE APP_INDEX;
