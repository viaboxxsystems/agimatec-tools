-- Tables for History with ${dbms}
-- File generated by "${generator.templateName}"

-- *** DROP Tables ***
/*
DROP TABLE H_JOURNAL;
<#list config.tables as histTable>
DROP TABLE ${histTable.historyTable};
</#list>
*/

-- *** CREATE Tables ***
CREATE TABLE H_JOURNAL (
   ID INTEGER NOT NULL,
   HIST_TABLE VARCHAR(50) NOT NULL,
   HIST_TIME TIMESTAMP NOT NULL,
   HIST_CONTEXTID VARCHAR(40),
   CONSTRAINT H_JOURNAL_PK PRIMARY KEY (ID, HIST_TABLE) USING INDEX TABLESPACE APP_INDEX
);

<#list config.tables as histTable>
CREATE TABLE ${histTable.historyTable} (<#list histTable.excludeColumns as each>
   -- exclude: ${each}</#list>
   VERSION INTEGER NOT NULL,
   <#assign table = catalog.getTable(histTable.tableName)>
   <#list table.columns as column>
   <#if !histTable.excludeColumns?seq_contains(column.columnName) && "version"!=column.columnName>
   ${column.columnName} ${column.typeName}<#if column.precisionEnabled>(${column.precision}<#if column.scale!=0>,${column.scale}</#if>)</#if><#if table.isPrimaryKeyColumn(column.columnName)> NOT NULL</#if>,
   </#if></#list>
   HIST_TIME TIMESTAMP NOT NULL,
   HIST_CONTEXTID VARCHAR(40),
   HIST_TYPE CHAR(1) NOT NULL,
   CONSTRAINT ${histTable.historyTable}_PK PRIMARY KEY (VERSION, <#list table.primaryKey.columns as pkColumn>${pkColumn}<#if pkColumn_has_next>, </#if></#list>) USING INDEX TABLESPACE APP_INDEX
);

</#list>

