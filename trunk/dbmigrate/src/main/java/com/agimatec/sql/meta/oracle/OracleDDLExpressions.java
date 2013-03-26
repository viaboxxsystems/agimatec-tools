package com.agimatec.sql.meta.oracle;

import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.DDLScriptSqlMetaFactory;
import com.agimatec.sql.meta.script.ExtractExpr;

/**
 * <b>Description:</b>
 * Build a CatalogDescription by parsing a SQL DDL script.
 * Caution: this class not not thread-safe. <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
public class OracleDDLExpressions extends DDLExpressions {
    /**
     * nun folgen die syntax-formate von den statements, die in den scripten erkannt und verarbeitet werden sollen:
     */
    private static final String[] statementFormats = {
            // "ALTER TABLE BIBECANCELLATION ADD (  ArcID INTEGER NULL,  COID char(17) NULL,  TOID char(17) NULL )"
            "{table-add-columns ALTER TABLE ${table} ADD '(' {columndefinition ${column} ${typeName} " +
                    "[{precision '(' {numbers ${value}...','} ')'}] [${mandatory(NOT NULL)}]...','} ')'}",
            // ilb
            // "ALTER TABLE Customer ADD(CONSTRAINT Cust_name UNIQUE( firstname, lastname) USING INDEX TABLESPACE SAMPLE_IDX)"
            "{table-add-constraint ALTER TABLE ${table} ADD '(' " +
                    "{constraint CONSTRAINT ${constraintName} [${unique(UNIQUE)}] '(' {columns ${column}...','} ')' " +
                    "[{tableSpace USING INDEX TABLESPACE ${tableSpace}]}] ')'}", // ilb
            // "CREATE UNIQUE INDEX RENTALCARSTAT_IDX_CRS_STAT    ON RENTALCARSTATION(CRSTYPE ASC, STATIONID) TABLESPACE "DB_INDEX""
            "{create-index CREATE [${unique(UNIQUE)}] INDEX ${indexName} ON ${table} '(' {columns ${column} [{func '(' {elements ${each}...','} ')'}] [ASC] [${desc(DESC)}]...','} ')' " +
                    "[{tableSpace TABLESPACE ${tableSpace}}] }", // ilb
            // ALTER TABLE Customer ADD (CONSTRAINT "Customer_Company" FOREIGN KEY ("COMPANYID") REFERENCES CLIENTORGUNIT ("OBJECTIDENTIFIER"))
            "{table-add-foreign-key ALTER TABLE ${table} ADD  " +
                    "{constraint CONSTRAINT ${constraintName} FOREIGN KEY '(' {columns ${column}...','} ')' " +
                    "REFERENCES ${refTable} [{refcolumns '(' {refcolumns ${column}...','} ')'}] " +
                    "[ON DELETE ${onDeleteRule}]" +
                    "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }", // ilb
            //"CREATE SEQUENCE SEQ_NLSBundle START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOORDER CACHE 100"
            "{create-sequence CREATE SEQUENCE ${sequence} [{attributes START WITH ${start} INCREMENT BY ${increment} " +
                    "[${nomaxvalue(NOMAXVALUE)}] [${nominvalue(NOMINVALUE)}] [${nocycle(NOCYCLE)}] " +
                    "[${noorder(NOORDER)}] [{cache CACHE ${value}}]}]}", // ilb
            //"CREATE TABLE Rate (PRICE NUMBER(9,2) NOT NULL, PRICE2 NUMBER(2), PRICE3 INTEGER, PRICE4 CHAR)"
            "{dezign-create-table CREATE TABLE ${table} '(' " + "{tableElement " +
                    "[{tableConstraint [{constraint CONSTRAINT ${constraintName}}] [${isPK(PRIMARY KEY)}] [${isUnique(UNIQUE)}] '(' {columns ${column}...','} ')' " +
                    "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                    "[{foreignKey FOREIGN KEY '(' {columns ${column}...','} ')' " +
                    "REFERENCES ${refTable} [{refcolumns '(' {refcolumns ${column}...','} ')' }] " +
                    "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                    "[{columndefinition ${column} ${typeName} [${varying(VARYING)}]" +
                    "[{precision '(' {numbers ${value}...','} [CHAR]')'}] " +
                    "[{default DEFAULT ${defaultValue}}] " +
                    "[{constraint CONSTRAINT ${constraintName}}] " +
                    "[${mandatory(NOT NULL)}] [${isUnique(UNIQUE)}]}] " + "...','} ')'}",
            "{create-table CREATE TABLE ${table} '(' " + "{tableElement " +
                                "[{primaryKey PRIMARY KEY '(' {columns ${column}...','} ')' " +
                                "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                                "[{foreignKey FOREIGN KEY '(' {columns ${column}...','} ')' " +
                                "REFERENCES ${refTable} '(' {refcolumns ${column}...','} ')' " +
                                "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                                "[{columndefinition ${column} ${typeName} " +
                                "[{precision '(' {numbers ${value}...','} [CHAR]')'}] [${mandatory(NOT NULL)}] [${isUnique(UNIQUE)}]}] " +
                                "...','} ')'}",
            // "ALTER TABLE NLSTEXT ADD (CONSTRAINT "NLSTEXT_PK" PRIMARY KEY (BUNDLEID, LOCALE, KEY) USING INDEX TABLESPACE "DB_INDEX")"
            "{table-add-primary-key ALTER TABLE ${table} ADD '(' " +
                    "{constraint CONSTRAINT ${constraintName} PRIMARY KEY '(' {columns ${column}...','} ')' " +
                    "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] ')'}",  // ilb
            //COMMENT ON TABLE User_Core IS 'Speichert die User-Daten '
            "{table-comment COMMENT ON TABLE ${table} IS ${comment{'}}}",
            //COMMENT ON COLUMN User_Core.gender IS 'MALE or FEMALE or null as GenderEnum'
            "{column-comment COMMENT ON COLUMN ${tableColumn} IS ${comment{'}}"
    };

    public static final ExtractExpr[] expressions;

    static {
        expressions = DDLScriptSqlMetaFactory.compileExpressions(statementFormats);
    }

    public ExtractExpr[] getExpressions() {
        return expressions;
    }

    /**
     * equalize type names (between schema-import and ddl-parsing)
     * @param cd
     */
    public void equalizeColumn(ColumnDescription cd) {
        if (cd.getTypeName().equalsIgnoreCase("INTEGER")) {
            cd.setTypeName("NUMBER");
        } else if (cd.getTypeName().equalsIgnoreCase("VARCHAR2")) {
            cd.setTypeName("VARCHAR");
        } else if (cd.getTypeName().equalsIgnoreCase("timestamp")) {
            cd.setTypeName(
                    "TIMESTAMP(6)"); // oracle delivers TIMESTAMP(6) when column is created as TIMESTAMP
        } else if (cd.getTypeName().equalsIgnoreCase("double")) {
            cd.setTypeName(
                    "FLOAT"); // oracle delivers FLOAT when column is created as DOUBLE
        } else if (cd.getTypeName().equalsIgnoreCase("CHARACTER")) {
            cd.setTypeName("CHAR");
        }

    }
}
