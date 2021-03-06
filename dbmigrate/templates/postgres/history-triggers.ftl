-- Triggers for History with ${dbms}
-- File generated by "${generator.templateName}"

-- *** DROP Triggers ***

/*
<#list config.tables as histTable>
<#if histTable.insertTrigger != ''>
DROP TRIGGER ${histTable.insertTrigger} ON ${histTable.tableName};
DROP FUNCTION ${histTable.insertTrigger}();
</#if>
<#if histTable.updateTrigger != ''>
DROP TRIGGER ${histTable.updateTrigger} ON ${histTable.tableName};
DROP FUNCTION ${histTable.updateTrigger}();
</#if>

</#list>
*/

-- *** CREATE trigger-functions ***
<#list config.tables as histTable>
<#assign table = catalog.getTable(histTable.tableName)>
-- ** Functions for ${histTable.tableName}
<#if histTable.insertTrigger != ''>
CREATE OR REPLACE FUNCTION ${histTable.insertTrigger}()
  RETURNS trigger AS  '
  BEGIN
    INSERT INTO H_JOURNAL (ID, HIST_TIME, HIST_CONTEXTID, HIST_TABLE)
    VALUES (NEW.${table.primaryKey.getColumn(0)}, now(), getContextId(), ''${histTable.tableName?lower_case}'');
    RETURN NEW;
  END;' LANGUAGE plpgsql;
/
</#if>
<#if histTable.updateTrigger != ''>

CREATE OR REPLACE FUNCTION ${histTable.updateTrigger}()
  RETURNS trigger AS '
  BEGIN
  IF(TG_OP=''DELETE'') THEN
    INSERT INTO ${histTable.historyTable} (
    <#list table.columns as column><#if !histTable.excludeColumns?seq_contains(column.columnName)>  ${column.columnName},
    </#if></#list>  HIST_TIME, HIST_CONTEXTID, HIST_TYPE)
    VALUES (
    <#list table.columns as column><#if !histTable.excludeColumns?seq_contains(column.columnName)>  OLD.${column.columnName},
    </#if></#list>  NOW(), getContextId(), ''D'');
    RETURN OLD;
  ELSIF (TG_OP=''UPDATE'' AND OLD.VERSION != NEW.VERSION) THEN
    INSERT INTO ${histTable.historyTable} (
    <#list table.columns as column><#if !histTable.excludeColumns?seq_contains(column.columnName)>  ${column.columnName},
    </#if></#list>  HIST_TIME, HIST_CONTEXTID, HIST_TYPE)
    VALUES (
    <#list table.columns as column><#if !histTable.excludeColumns?seq_contains(column.columnName)>  OLD.${column.columnName},
    </#if></#list>  NOW(), getContextId(), ''U'');
  END IF;
  RETURN NEW;
  END;' LANGUAGE plpgsql;
/  
</#if>

</#list>

-- *** CREATE Triggers ***
/* Postgres says: If you have no specific reason to make a trigger before or after,
   the before case is more efficient, since the information about the operation doesn't
   have to be saved until end of statement. */ 

<#list config.tables as histTable>
-- ** Triggers for ${histTable.tableName}
<#if histTable.insertTrigger != ''>
CREATE TRIGGER ${histTable.insertTrigger}
  AFTER INSERT ON ${histTable.tableName}
  FOR EACH ROW
  EXECUTE PROCEDURE ${histTable.insertTrigger}();
/
</#if>
<#if histTable.updateTrigger != ''>

CREATE TRIGGER ${histTable.updateTrigger}
  AFTER DELETE OR UPDATE
  ON ${histTable.tableName}
  FOR EACH ROW
  EXECUTE PROCEDURE ${histTable.updateTrigger}();
/
</#if>

</#list>

