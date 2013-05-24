/* ---------------------------------------------------------------------- */
/* Table "${table.tableName}" */
/* ---------------------------------------------------------------------- */

CREATE TABLE ${table.tableName} (
<#list table.columns as column>
    <#include "print-column.ftl"><#if table.columns?last!=column>,
</#if></#list><#if table.primaryKey??>,
    <#if table.primaryKey.indexName??>CONSTRAINT ${table.primaryKey.indexName}</#if> PRIMARY KEY (<#list generator.exeptLast(table.primaryKey.columns) as pkCol>${pkCol}, </#list>${generator.last(table.primaryKey.columns)}) USING INDEX TABLESPACE APP_INDEX<#list table.constraints as index>,
    <#include "print-constraint.ftl"></#list></#if>
);

<#list table.indices as index>
<#include "create-index.ftl">
</#list>

<#if (table.comment)??>COMMENT ON TABLE ${table.tableName} IS '${table.comment}';</#if>
<#list table.columns as column><#if column.comment??>COMMENT ON COLUMN ${table.tableName}.${column.columnName} IS '${column.comment}';
</#if></#list>
