ALTER TABLE ${table.tableName} ADD CONSTRAINT ${foreignKey.constraintName}
    FOREIGN KEY (<#list foreignKey.columns as col>${col}<#if foreignKey.columns?last != col>, </#if></#list>) REFERENCES ${foreignKey.refTableName} (<#list foreignKey.refColumns as col>${col}<#if foreignKey.refColumns?last != col>, </#if></#list>)<#if
    foreignKey.onDeleteRule??> ON DELETE ${foreignKey.onDeleteRule}</#if>;

