${column.columnName} ${column.typeName}<#if column.isPrecisionEnabled()>(${column.precision}<#if column.scale!=0>,${column.scale}</#if>)</#if><#if (column.defaultValue)??> DEFAULT ${column.defaultValue}</#if><#if !column.isNullable()> NOT NULL</#if>