package com.agimatec.sql.meta.mysql;

import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.DDLScriptSqlMetaFactory;
import com.agimatec.sql.meta.script.ExtractExpr;

/**
 * <p>syntax expressions to parse MySql scripts</p>
 * User: roman.stumm@viaboxx.de<br>
 * Date: 26.03.13
 */
public class MySqlDDLExpressions extends DDLExpressions {
    /**
     * nun folgen die syntax-formate von den statements, die in den scripten erkannt und verarbeitet werden sollen:
     */
    private static final String[] statementFormats = {
            // limitation - create-view, drop-view missing (alter-view missing)
            // limitation - cannot detect: "ALTER TABLE t alter column c1 set not null, add primary key (c);"
            // use "ALTER TABLE ..., add constraint t_pkey primary key (c);" instead

            // "ALTER TABLE Customer ADD CONSTRAINT Cust_name UNIQUE( firstname, lastname) USING INDEX TABLESPACE SAMPLE_IDX"
            "{table-add-constraint ALTER TABLE ${table} ADD " +
                    "{constraint CONSTRAINT ${constraintName} [${unique(UNIQUE)}] '(' {columns ${column}...','} ')' " +
                    "[{tableSpace USING INDEX TABLESPACE ${tableSpace}]}]}", // ilb
            // "CREATE UNIQUE INDEX RENTALCARSTAT_IDX_CRS_STAT    ON RENTALCARSTATION(CRSTYPE ASC, STATIONID) TABLESPACE "DB_INDEX""
            "{create-index CREATE [${unique(UNIQUE)}] INDEX ${indexName} ON ${table} '(' {columns ${column} [{func '(' {elements ${each}...','} ')'}] [ASC] [${desc(DESC)}]...','} ')' " +
                    "[{tableSpace TABLESPACE ${tableSpace}}] }", // ilb
            // ALTER TABLE Address ADD CONSTRAINT CV_Country_Address_FK_1 FOREIGN KEY (country) REFERENCES CV_Country (country_id);
            "{table-add-foreign-key ALTER TABLE ${table} ADD " +
                    "{constraint CONSTRAINT ${constraintName} FOREIGN KEY '(' {columns ${column}...','} ')' " +
                    "REFERENCES ${refTable} [{refcolumns '(' {refcolumns ${column}...','} ')'}] " +
                    "[ON DELETE ${onDeleteRule}]" +
                    "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }", // ilb
            //"CREATE SEQUENCE SEQ_NLSBundle INCREMENT 1 START 1 NOMAXVALUE NOMINVALUE NOCYCLE NOORDER CACHE 100"
            "{create-sequence CREATE SEQUENCE ${sequence} [{attributes INCREMENT [BY] ${increment} START [WITH] ${start} " +
                    "[${nomaxvalue(NOMAXVALUE)}] [${nominvalue(NOMINVALUE)}] [${nocycle(NOCYCLE)}] " +
                    "[${noorder(NOORDER)}] [{cache CACHE ${value}}]}]}", // ilb
            //"CREATE TABLE Rate (PRICE NUMBER(9,2) NOT NULL, PRICE2 NUMBER(2), PRICE3 INTEGER, PRICE4 CHAR)"
            "{dezign-create-table CREATE TABLE ${table} '(' " + "{tableElement " +
                    // support: "constraint MY_TABLE_FK foreign key (FK_COL_NAME) references REFERENCED_TABLE(ID_COL_NAME)"
                    "[{foreignKey [{constraint CONSTRAINT ${constraintName}}] FOREIGN KEY '(' {columns ${column}...','} ')' " +
                    "REFERENCES ${refTable} [{refcolumns '(' {refcolumns ${column}...','} ')'}] }]" +
                    "[{tableConstraint [{constraint CONSTRAINT ${constraintName}}] [${isPK(PRIMARY KEY)}] [${isUnique(UNIQUE)}] '(' {columns ${column}...','} ')' }]" +
                    "[{columndefinition ${column} ${typeName} [${varying(VARYING)}]" +
                    "[{precision '(' {numbers ${value}...','} [CHAR]')'}] " +
                    "[{default DEFAULT ${defaultValue}}] " +
                    "[{constraint CONSTRAINT ${constraintName}}] " +
                    "[${mandatory(NOT NULL)}] [{default DEFAULT ${defaultValue}}] [${isUnique(UNIQUE)}] [${isPK(PRIMARY KEY)}]}] " + "...','} ')'}",
            "{create-table CREATE TABLE ${table} '(' " + "{tableElement " +
                    "[{primaryKey PRIMARY KEY '(' {columns ${column}...','} ')' " +
                    "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                    "[{foreignKey FOREIGN KEY '(' {columns ${column}...','} ')' " +
                    "REFERENCES ${refTable} [{refcolumns '(' {refcolumns ${column}...','} ')'}] " +
                    "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                    "[{columndefinition ${column} ${typeName} " +
                    "[{precision '(' {numbers ${value}...','} [CHAR]')'}] [${mandatory(NOT NULL)}] [${isUnique(UNIQUE)}] [${isPK(PRIMARY KEY)}]}] " +
                    "...','} ')'}",
            // "ALTER TABLE NLSTEXT ADD CONSTRAINT "NLSTEXT_PK" PRIMARY KEY (BUNDLEID, LOCALE, KEY) USING INDEX TABLESPACE "DB_INDEX""
            "{table-add-primary-key ALTER TABLE ${table} ADD " +
                    "{constraint CONSTRAINT ${constraintName} PRIMARY KEY '(' {columns ${column}...','} ')' " +
                    "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }",  // ilb
            //COMMENT ON TABLE User_Core IS 'Speichert die User-Daten '
            "{table-comment COMMENT ON TABLE ${table} IS ${comment{'}}}",
            //COMMENT ON COLUMN User_Core.gender IS 'MALE or FEMALE or null as GenderEnum'
            "{column-comment COMMENT ON COLUMN ${tableColumn} IS ${comment{'}}",
            // ALTER TABLE test
            //  alter column col2 TYPE varchar(200),
            //  alter column col3 type varchar(150),
            //  add col4 integer not null,
            //  alter column col5 drop not null,
            //  alter col6 set not null
            "{table-alter-columns ALTER TABLE ${table} {tableElement" +
                    "[{alter-column-set-notnull ALTER [COLUMN] ${column} SET NOT NULL}]" +

                    "[{alter-column-drop-notnull ALTER [COLUMN] ${column} DROP NOT NULL}]" +

                    "[{constraint ADD CONSTRAINT ${constraintName} [${unique(UNIQUE)}] '(' {columns ${column}...','} ')' " +
                    "}]" +

                    "[{add-foreign-key ADD CONSTRAINT ${constraintName} FOREIGN KEY '(' {columns ${column}...','} ')' " +
                    "REFERENCES ${refTable} [{refcolumns '(' {refcolumns ${column}...','} ')'}] " +
                    "[ON DELETE ${onDeleteRule}]" +
                    "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +

                    "[{drop-constraint DROP CONSTRAINT ${constraintName}}]" +
                    "[{drop-column DROP [COLUMN] ${column}}]" +

                    "[{add-column ADD [COLUMN] ${column} ${typeName} [${varying(VARYING)}]" +
                    "[{default DEFAULT ${defaultValue}}] " +
                    "[{constraint CONSTRAINT ${constraintName}}] " +
                    "[{precision '(' {numbers ${value}...','} ')'}] [${mandatory(NOT NULL)}]}] " +

                    "[{alter-column-type ALTER [COLUMN] ${column} TYPE ${typeName} [${varying(VARYING)}]" +
                    "[{default DEFAULT ${defaultValue}}] " +
                    "[{constraint CONSTRAINT ${constraintName}}] " +
                    "[{precision '(' {numbers ${value}...','} ')'}] [${mandatory(NOT NULL)}]}]" +

                    "...','}}",
            // DROP TRIGGER TR_I_User_Core ON User_Core
            "{drop-trigger DROP TRIGGER ${trigger} ON ${table}}",
            "{drop-table DROP TABLE [IF EXISTS] ${table} [CASCADE]}",
            "{drop-sequence DROP SEQUENCE ${sequence}}"};

    public static final ExtractExpr[] expressions;

    static {
        expressions = DDLScriptSqlMetaFactory.compileExpressions(statementFormats);
    }

    @Override
    public ExtractExpr[] getExpressions() {
        return expressions;
    }

    /**
     * equalize type names
     */
    @Override
    public void equalizeColumn(ColumnDescription cd) {
        // do nothing
    }

    @Override
    public String strip(String value) {
        if (value == null) return null;
        int start = 0, end = value.length();
        if (value.startsWith("`")) {
            start++;
        }
        if (value.endsWith("`")) {
            end--;
        }
        return value.substring(start, end);
    }
}
