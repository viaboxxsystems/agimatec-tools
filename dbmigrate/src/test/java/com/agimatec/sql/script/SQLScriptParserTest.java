package com.agimatec.sql.script;

import com.agimatec.commons.util.ClassUtils;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * <p>Title: Agimatec GmbH</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Agimatec GmbH </p>
 *
 * @author Roman Stumm
 */
public class SQLScriptParserTest extends TestCase {

    private static final Logger myLogger = LoggerFactory.getLogger(SQLScriptParser.class);

    private ScriptVisitorDummy visitor;
    private SQLScriptParser parser;

    public SQLScriptParserTest(String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(SQLScriptParserTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        visitor = new ScriptVisitorDummy();
        parser = new SQLScriptParser("", myLogger);
    }

    int commitCount = 0, rollbackCount = 0;
    int lineIdx = 0;
    public void testIterateSQLLines() throws IOException, SQLException {
        final String[] lines = {
                "create table dummy()",
                "alter table dummy",
                "drop table dummy",
                "select from dummy"
        };

        parser.iterateSQLLines(new ScriptVisitor() {
            public int visitStatement(String statement) throws SQLException {
                assertEquals(lines[lineIdx++], statement);
                return 1;
            }

            public void visitComment(String theComment) throws SQLException {
                assertEquals("-- found a comment", theComment);
            }

            public void doCommit() throws SQLException {
                commitCount++;
            }

            public void doRollback() throws SQLException {
                rollbackCount++;
            }
        }, "cp://script_without_semicolon.sql");
        assertEquals(1, commitCount);
        assertEquals(1, rollbackCount);
    }

    public void testParseFromClasspath() throws Exception {
        SQLScriptParser theParser = new SQLScriptParser(null, myLogger);
        theParser.iterateSQLScript(visitor, ClassUtils.getClassLoader().getResource("testscript.sql"));
    }

    public void testParseFromFile() throws SQLException, IOException {
        SQLScriptParser theParser = new SQLScriptParser("./src/test/", myLogger);
        theParser.iterateSQLScript(visitor, "sqlscriptfiletest.sql");
    }

    public void testParseLiteral() throws IOException, SQLException {
        String sql = "select * from customer where name = 'Lit''eral ';";
        parser.iterateSQL(visitor, sql);
        assertEquals("select * from customer where name = 'Lit''eral '", visitor.getStatements().get(0));
    }

    public void testParseLiteral3() throws Exception {
        String sql =
                "insert into jasper_report (jasper_report_id, name, description) values(1,'Automatenauslastung','Auslastung der Automaten');";
        parser.iterateSQL(visitor, sql);
        assertEquals(
                "insert into jasper_report (jasper_report_id, name, description) values(1,'Automatenauslastung','Auslastung der Automaten')",
                visitor.getStatements().get(0));
    }

    public void testParseLiteral2() throws IOException, SQLException {
        parser.iterateSQL(visitor, "select 'unclosed' from dual");
        assertEquals("select 'unclosed' from dual", visitor.getStatements().get(0));
    }

    public void testCommitRollback() throws IOException, SQLException {
        parser.iterateSQL(visitor, "  commit;\ncommit;\nrollback  ;\nrollback --test;\n");
        assertEquals(2, visitor.getCommits());
        assertEquals(2, visitor.getRollbacks());
    }

    public void testEmptyStatements() throws SQLException, IOException {
        parser.iterateSQL(visitor, "SELECT * FROM DUAL;   ;\n  ; ;\n;");
        assertEquals("SELECT * FROM DUAL", visitor.getStatements().get(0));
        assertEquals(1, visitor.getStatements().size());
    }

    public void testParseMultiLineCommentEmbedded() throws SQLException, IOException {
        String sql = "select * from customer/* jetzt kommts\nknueppeldick\n*/ where lastname='Mueller';commit;";
        parser.iterateSQL(visitor, sql);
        assertEquals(1, visitor.getComments().size());
        assertEquals(1, visitor.getStatements().size());
        assertEquals(1, visitor.getCommits());
        assertEquals("select * from customer where lastname='Mueller'", visitor.getStatements().get(0));
        assertEquals("/* jetzt kommts\nknueppeldick\n*/", visitor.getComments().get(0));
    }

    public void testSingleLineCommentEmbedded() throws SQLException, IOException {
        String sql = "select * from customer -- jetzt kommts\nwhere lastname='Mueller';\ncommit;\n   -- bestaetigen\n";
        parser.iterateSQL(visitor, sql);
        assertEquals(2, visitor.getComments().size());
        assertEquals(1, visitor.getCommits());
        assertEquals(1, visitor.getStatements().size());
        assertEquals("select * from customer where lastname='Mueller'", visitor.getStatements().get(0));
        assertEquals("-- jetzt kommts", visitor.getComments().get(0));
    }

    public void testSingleLineCommentEmpty() throws SQLException, IOException {
        String sql = "--\n-- a test\n--\nSELECT * FROM DUAL;";
        parser.iterateSQL(visitor, sql);
        assertEquals(1, visitor.getStatements().size());
        assertEquals(3, visitor.getComments().size());
        assertEquals("SELECT * FROM DUAL", visitor.getStatements().get(0));
    }

    public void testSpaceTrimming() throws SQLException, IOException {
        String sql = "  select * from customer \nwhere lastname='Mueller';\ncommit;\n   \n";
        parser.iterateSQL(visitor, sql);
        assertEquals("select * from customer where lastname='Mueller'", visitor.getStatements().get(0));
        assertEquals(1, visitor.getCommits());
        assertEquals(1, visitor.getStatements().size());
    }

    public void testOtherDelimeter() throws SQLException, IOException {
        String sql = "select sysdate from dual\n/\nselect count(*) from dual;\n";
        parser.iterateSQL(visitor, sql);
        assertEquals(2, visitor.getStatements().size());
        assertEquals("select sysdate from dual", visitor.getStatements().get(0));
        assertEquals("select count(*) from dual", visitor.getStatements().get(1));

        visitor.reset();
        sql = "select sysdate from dual\n/       \nselect count(*) from dual;\n";
        parser.iterateSQL(visitor, sql);
        assertEquals(2, visitor.getStatements().size());
        assertEquals("select sysdate from dual", visitor.getStatements().get(0));
        assertEquals("select count(*) from dual", visitor.getStatements().get(1));
    }

    public void testParseMultiLineComment() throws SQLException, IOException {
        String sql = "/**\n\r" + " * ON INSERT;UPDATE;DELETE Trigger\n\r" + " **/\r\n"
                + "CREATE OR REPLACE TRIGGER SH_Airport\n" + "AFTER INSERT OR UPDATE OF \"CODE\" OR DELETE ON Airport\n"
                + "REFERENCING OLD AS OLD NEW AS NEW\n" + "FOR EACH ROW BEGIN\n" + " IF INSERTING THEN\n"
                + "   INSERT INTO SH_AIRPORT_CODE (Code) VALUES (:NEW.Code);\n"
                + " ELSIF UPDATING AND :NEW.Code<>:OLD.Code THEN\n"
                + "    UPDATE SH_AIRPORT_CODE SET Code = :NEW.Code WHERE CODE = :OLD.Code;\n" + " ELSIF DELETING THEN\n"
                + "    DELETE FROM SH_AIRPORT_CODE WHERE Code = :OLD.Code;\n" + " END IF;\n" + "END;\n" + "/";
        parser.iterateSQL(visitor, sql);
        assertEquals(1, visitor.getComments().size());
        assertEquals("/**\n\r * ON INSERT;UPDATE;DELETE Trigger\n\r **/", visitor.getComments().get(0));
        String stmt = (String) visitor.getStatements().get(0);
        assertTrue(stmt.startsWith("CREATE OR REPLACE TRIGGER SH_Airport"));
        assertTrue(stmt.endsWith("END;"));
    }

    public void testParseSingleLineComment() throws SQLException, IOException {
        String sql = "-- persistence 'ClientProfile'\n" + "CREATE TABLE ClientProfile("
                + "ObjectIdentifier char(17) NOT NULL ," + "  ObjectVersion integer NULL ," +
                "  ClientID char(17) NOT NULL ,"
                + " Name varchar(50) NOT NULL ," + "  ActiveState number(1) NOT NULL ," + "  ValidFrom date NULL ,"
                + "  ValidTo date NULL ," + "  Notice varchar(255) NULL" + ");";
        parser.iterateSQL(visitor, sql);
        assertEquals(1, visitor.getComments().size());
        assertEquals("-- persistence 'ClientProfile'", visitor.getComments().get(0));
        assertEquals(1, visitor.getStatements().size());
        String stmt = (String) visitor.getStatements().get(0);
        assertTrue(stmt.startsWith("CREATE TABLE ClientProfile("));
        assertTrue(stmt.endsWith("Notice varchar(255) NULL)"));
    }

    public void testParseSELECT() throws Exception {
        parser.iterateSQL(visitor, "SELECT * FROM TEST\n\n;\r\nSELECT * FROM TEST2;");
        assertEquals(2, visitor.getStatements().size());
        assertEquals("SELECT * FROM TEST", visitor.getStatements().get(0));
        assertEquals("SELECT * FROM TEST2", visitor.getStatements().get(1));
    }

    public void testParseBrokenSQL() throws SQLException, IOException {
        String sql = "ALTER TABLE CUSTRAILCARD ADD (CONSTRAINT \"CustRailCard_FK0\" FOREIGN\r\n"
                + "KEY (\"CUSTOMERID\")\r\n" + "REFERENCES CUSTOMER (\"OBJECTIDENTIFIER\"));";
        String expectedSql =
                "ALTER TABLE CUSTRAILCARD ADD (CONSTRAINT \"CustRailCard_FK0\" FOREIGN KEY (\"CUSTOMERID\") REFERENCES CUSTOMER (\"OBJECTIDENTIFIER\"))";
        parser.iterateSQL(visitor, sql);
        assertEquals(expectedSql, visitor.getStatements().get(0));
    }

    /**
     * a table name with trigger in its name
     */
    public void testParseTriggerTableName() throws SQLException, IOException {
        String theSQL =
                "CREATE TABLE my_trigger ( TripBegin VARCHAR(100));CREATE TABLE my_procedure( BeginTrip VARCHAR(100));CREATE TABLE test(\tBeginTrip\tVARHCHAR(100));commit;";
        parser.iterateSQL(visitor, theSQL);
        assertEquals(3, visitor.getStatements().size());
        assertEquals("CREATE TABLE my_trigger ( TripBegin VARCHAR(100))", visitor.getStatements().get(0));
        assertEquals("CREATE TABLE my_procedure( BeginTrip VARCHAR(100))", visitor.getStatements().get(1));
        assertEquals("CREATE TABLE test( BeginTrip VARHCHAR(100))", visitor.getStatements().get(2));
    }

    public void testParseTrigger() throws Exception {
        String theTriggerSQL =
                "CREATE OR REPLACE TRIGGER TR_ACTHISTORYENTRY \nBEFORE\nINSERT\nBEGIN\nSELECT SEQ_ACTHISTORY.NEXTVAL INTO :new.SeqNumber FROM DUAL;\nEND;\n/";
        String theParseSQL =
                "CREATE OR REPLACE TRIGGER TR_ACTHISTORYENTRY BEFORE INSERT BEGIN\nSELECT SEQ_ACTHISTORY.NEXTVAL INTO :new.SeqNumber FROM DUAL;\nEND;";
        parser.iterateSQL(visitor, theTriggerSQL);
        assertEquals(theParseSQL, visitor.getStatements().get(0));
    }

    public void testParseTrigger2() throws Exception {
        String theSQL = "CREATE TRIGGER Test\tBEGIN\tSELECT * FROM DUAL; SELECT MORE;END\r\n/\n\rSELECT * FROM DUAL;";
        parser.iterateSQL(visitor, theSQL);
        assertEquals(2, visitor.getStatements().size());
        assertEquals("CREATE TRIGGER Test BEGIN SELECT * FROM DUAL; SELECT MORE;END\n\n",
                visitor.getStatements().get(0));
        assertEquals("SELECT * FROM DUAL", visitor.getStatements().get(1));
    }

    public void testParseTriggerWithComment() throws Exception {
        String theTriggerSQL =
                "CREATE OR REPLACE TRIGGER TR_ACTHISTORYENTRY \nBEFORE\nINSERT\n/*TR_ACTHISTORYENTRY*/\nBEGIN\nSELECT SEQ_ACTHISTORY.NEXTVAL INTO :new.SeqNumber FROM DUAL;\r\nEND;\n/";
        String theParseResult =
                "CREATE OR REPLACE TRIGGER TR_ACTHISTORYENTRY BEFORE INSERT BEGIN\nSELECT SEQ_ACTHISTORY.NEXTVAL INTO :new.SeqNumber FROM DUAL;\nEND;";
        parser.iterateSQL(visitor, theTriggerSQL);
        //assertEquals(1, visitor.getStatements().size());
        assertEquals(theParseResult, visitor.getStatements().get(0));
    }

    public void testParseTriggerWithComment2() throws Exception {
        String theTriggerSQL =
                "CREATE OR REPLACE TRIGGER TR_ACTHISTORYENTRY \nBEFORE\nINSERT\n/*TR_ACTHISTORYENTRY\r\n-----\r\n*/\nBEGIN\nSELECT SEQ_ACTHISTORY.NEXTVAL INTO :new.SeqNumber FROM DUAL;\r\nEND;\r\n/";
        String theParseResultSQL =
                "CREATE OR REPLACE TRIGGER TR_ACTHISTORYENTRY BEFORE INSERT BEGIN\nSELECT SEQ_ACTHISTORY.NEXTVAL INTO :new.SeqNumber FROM DUAL;\nEND;";
        parser.iterateSQL(visitor, theTriggerSQL);
        //assertEquals(1, visitor.getStatements().size());
        assertEquals(theParseResultSQL, visitor.getStatements().get(0));
    }

    public void testParseXMLValueInSQL() throws SQLException, IOException {
        String theXMLSQL = "insert into membershipresolver (resultdisplayconfig) values ("
                +
                "(select objectidentifier from btmclient where identity='BAHN'),'<config><ArrayList name=\"test\"><String name=\"ValuePath\" value=\"Value\" /></ArrayList></config>');";

        parser.iterateSQL(visitor, theXMLSQL);
        assertEquals(theXMLSQL.substring(0, theXMLSQL.length() - 1), visitor.getStatements().get(0));
    }

    /**
     * test if properties in the script are correctly replaced
     */
    public void testReplaceProperties() throws SQLException, IOException {
        String theSQL = "select * from ${user}.table@${link} where oid is not null and date=${datetime()};";
        Properties aMap = new Properties();
        aMap.put("user", "Admin");
        aMap.put("link", "SourceDB");
        aMap.put("datetime()", "040301");
        parser.setEnvironment(aMap);
        parser.iterateSQL(visitor, theSQL);
        assertEquals("select * from Admin.table@SourceDB where oid is not null and date=040301",
                visitor.getStatements().get(0));
    }

    public void testPLSQLDeclare() throws SQLException, IOException {
        String theSQL = "DECLARE\n" + "  CURSOR cCust IS\n" + "    SELECT * FROM custaddress\n"
                + "    ORDER BY customerid\n" + "    FOR UPDATE;\n" + "  vRec     cCust%ROWTYPE;\n"
                + "  vId      VARCHAR2(20) := 'X';\n" + "  vCounter NUMBER(4,0);\n" + "begin\n" +
                "  FOR vRec IN cCust LOOP\n"
                + "     IF vId != vRec.customerid THEN\n" + "        vCounter := 1;\n" + "     END IF;\n"
                + "     vId := vRec.customerid;\n" + "     UPDATE custaddress SET corder = vCounter\n"
                + "     WHERE objectidentifier = vRec.objectidentifier;\n" + "     vCounter := vCounter + 1;\n"
                + "  END LOOP;\n" + "  COMMIT;\n" + "END;\n" + "/";
        parser.iterateSQL(visitor, theSQL);
        assertEquals(1, visitor.getStatements().size());
    }

    public void testBigProcedure() throws Exception {
        String theSQL = "CREATE OR REPLACE PROCEDURE MigrationFF as\n" + "\n" + "   CURSOR cSd IS\n" + "      SELECT\n"
                + "         sd.objectidentifier,\n" + "         sd.btnumber,\n" + "         sd.sdShippingSetting,\n"
                + "         sd.companyId,\n" + "         sd.emailAddressForOriginal,\n" +
                "         sd.emailAddressesForCopy,\n"
                + "         sd.boState,\n" + "         au.agencyCountryId\n" + "      FROM\n" +
                "         Salesdocument sd,\n"
                + "         Company co,\n" + "         AgencyUnit au\n" + "      WHERE\n"
                + "             sd.companyId = co.objectidentifier\n" +
                "         AND co.agencyUnitId = au.objectidentifier;\n"
                + "\n" + "   CURSOR cSdr(salesDocumentIdIN IN char) IS\n" + "      SELECT *\n"
                + "      FROM SalesdocumentReceiver\n" + "      WHERE salesDocumentId = salesDocumentIdIN;\n" + "\n"
                + "   vRecSd      cSd%ROWTYPE;\n" + "   vRecSdr     cSdr%ROWTYPE;\n" + "\n" +
                "   ffBusinessTripId char(17);\n"
                + "   ffCompanyId char(17);\n" + "   ffTravellerPersonId char(17);\n" +
                "   ffCompanyIdForCl char(17);\n" + "\n"
                + "   ffContactLinkId char(17);\n" + "   ffAddressId char(17);\n" + "   ffFaxEmailId char(17);\n" + "\n"
                + "   deliveryTypeId char(17);\n" + "   salesDocumentId char(17);\n" + "\n" + "BEGIN\n" + "\n"
                + "   -- correct data where email address is not given,\n" + "   UPDATE salesdocument\n"
                + "      SET sdShippingSetting = 0\n" + "      WHERE\n" +
                "             emailAddressForOriginal IS NULL\n"
                + "         AND sdShippingSetting = 2;\n" + "\n" + "\n" + "   FOR vRecSd IN cSd LOOP\n" + "\n"
                + "      salesDocumentId := vRecSd.objectIdentifier;\n" + "      ffBusinessTripId := null;\n"
                + "      -- fill FFBusinessTrip\n" + "      IF vRecSd.btNumber IS NOT NULL THEN\n"
                + "         ffBusinessTripId := Oid.get('FFBusinessTrip');\n" + "\n" +
                "         insert into FFBUSINESSTRIP (\n"
                + "            OBJECTIDENTIFIER,\n" + "            OBJECTVERSION,\n" + "            BUSINESSTRIPID,\n"
                + "            TICKETDELIVERYLEVEL,\n" + "            VISAINFORMATION\n" + "        )\n" + "        (\n"
                + "            select\n" + "               ffBusinessTripId,\n" + "               0,\n"
                + "               bt.objectidentifier,\n" +
                "               0, -- set to default level businesstrip = 0\n"
                + "               0\n" + "            from\n" + "               businessTrip bt\n" +
                "            where\n"
                + "               vRecSd.btNumber = bt.tripNumber\n" + "        );\n" + "      END IF;\n" + "\n"
                + "      -- fill FFCompany\n" + "      ffCompanyId := OID.GET('FFCompany');\n" + "\n"
                + "      INSERT INTO FFCOMPANY (\n" + "         OBJECTIDENTIFIER,\n" + "         OBJECTVERSION,\n"
                + "         SDSHIPPINGSETTING,\n" + "         COMPANYID,\n" + "         FFBUSINESSTRIPID,\n"
                + "         TICKETDELIVERYTIME,\n" + "         INVOICEDELIVERYTIME,\n" +
                "         NUMBERCOPIESCOMPANY,\n"
                + "         NUMBERCOPIESPURCHASER,\n" + "         NUMBERCOPIESTRAVELLER,\n"
                + "         ADDITIONALCOPYRECEIVER,\n" + "         EMAILADDRESSFORORIGINAL,\n"
                + "         EMAILADDRESSESFORCOPY,\n" + "         MIDOFFICERELEVANT,\n"
                + "         ORIGINALINVOICEADDRESSTYPE ,\n" + "         TICKETDELIVERYTYPEID,\n"
                + "         INVOICEDELIVERYTYPEID\n" + "      )\n" + "      (\n" + "         select\n"
                + "            ffCompanyId,\n" + "            0,\n" + "            vRecSd.sdShippingSetting,\n"
                + "            vRecSd.companyId,\n" + "            ffBusinessTripId,\n" +
                "            co.ticketdeliverytime,\n"
                + "            mo.invoiceDeliveryTime,\n" + "            mo.numberCopiesCompany,\n"
                + "            mo.numberCopiesPurchaser,\n" + "            mo.numberCopiesTraveller,\n"
                + "            mo.additionalCopyReceiver,\n" + "            mo.emailAddressForOriginal,\n"
                + "            mo.emailAddressesForCopy,\n" + "            1, -- MIDOFFICERELEVANT\n"
                + "            mo.originalInvoiceAddressType,\n" + "            co.ticketDeliveryTypeId,\n"
                + "            mo.invoiceDeliveryTypeId\n" + "         from\n" + "            company co,\n"
                + "            companyMidOffice mo\n" + "         where\n"
                + "                vRecSd.companyId = co.objectidentifier\n"
                + "            and mo.companyId (+) = co.objectidentifier\n" + "      );\n" + "\n" + "--   wenn Paper\n"
                + "--     Für jeden SDR\n" + "--       ffAddressId = ?\n" + "--       lege FFAddress an\n"
                + "--       lege FFContactLink an\n" + "\n" + "-- alle sdr übernehmen, unabhängig von shippingsetting\n"
                + "--      IF vRecSd.sdShippingSetting = 0 THEN\n" + "\n"
                + "         FOR vRecSdr IN cSdr(vRecSd.objectIdentifier) LOOP\n"
                + "            ffCompanyIdForCl := ffCompanyId;\n" + "\n" + "            -- find deliveryTypeId\n"
                + "            deliveryTypeId := null;\n" + "\n" + "            ffAddressId := OId.get('FFAddress');\n"
                + "            ffFaxEmailId := null;\n" + "            INSERT INTO FFAddress\n" + "            (\n"
                + "               OBJECTIDENTIFIER,\n" + "               OBJECTVERSION,\n"
                + "               COMPANYLONGNAME1,\n" + "               COMPANYLONGNAME2,\n" +
                "               ORGUNITNAME,\n"
                + "               RECEIVERNAME,\n" + "               LINE1,\n" + "               LINE2,\n"
                + "               LINE3,\n" + "               LINE4,\n" + "               ZIPCODE,\n" +
                "               CITY,\n"
                + "               STATECODE,\n" + "               COUNTRYCODE,\n" + "               FFDELIVERYDATE,\n"
                + "               REMARK,\n" + "               NUMBERCOPIES,\n" + "               ORIGINID,\n"
                + "               FFDELIVERYTIME,\n" + "               DELIVERYTYPEID\n" + "            )\n"
                + "            VALUES\n" + "            (\n" + "               ffAddressId,\n" + "               0,\n"
                + "               vRecSdr.companylongname1,\n" + "               vRecSdr.companylongname2,\n"
                + "               vRecSdr.orgunitname,\n" + "               vRecSdr.receiverName,\n"
                + "               vRecSdr.line1,\n" + "               vRecSdr.line2,\n" +
                "               vRecSdr.line3,\n"
                + "               vRecSdr.line4,\n" + "               vRecSdr.zipCode,\n" +
                "               vRecSdr.city,\n"
                + "               vRecSdr.stateCode,\n" + "               vRecSdr.countryCode,\n"
                + "               null, -- ffDeliveryDate,\n"
                + "               vRecSdr.remark || ' (Org. Auslieferungstyp: ' || vRecSdr.FFDeliveryType || ')',\n"
                + "               vRecSdr.numberToSend,\n" +
                "               null, -- originId, we have no information\n"
                + "               null, -- ffDeliveryTime\n" + "               deliveryTypeId\n" + "            );\n" +
                "\n"
                + "            IF vRecSdr.ffAddressType in (3,8) THEN\n"
                + "              ffTravellerPersonId := OID.GET('FFTravellerPerson');\n"
                + "               ffCompanyIdForCL := null;\n" + "\n" +
                "               insert into FFTravellerPerson (\n"
                + "                  OBJECTIDENTIFIER,\n" + "                  OBJECTVERSION,\n"
                + "                  TRAVELLERPERSONID,\n" + "                  FFCOMPANYID,\n"
                + "                  ITINERARYSHIPPINGSETTING,\n" + "                  TRAVELLERNAME\n" +
                "               )\n"
                + "               VALUES\n" + "               (\n" + "                  ffTravellerPersonId,\n"
                + "                  0,\n" +
                "                  null,  -- there is no way to find the orig. travellerperson!\n"
                + "                  ffCompanyId,\n" + "                  0,\n"
                + "                  vRecSdr.companyLongName1 --receiverName -- ???\n" + "               );\n"
                + "            END IF;\n" + "\n" + "            insert into FFContactLink\n" + "            (\n"
                + "               OBJECTIDENTIFIER,\n" + "               OBJECTVERSION,\n" +
                "               FFADDRESSTYPE,\n"
                + "               FROMTRIPID,\n" + "               FROMCOMPANYID,\n" +
                "               FROMTRAVELLERID,\n"
                + "               TOADDRESSID,\n" + "               TOFAXEMAILID,\n" + "               ORDERNUMBER,\n"
                + "               FFSHIPPINGTYPE,\n" + "               SUPPRESSED,\n" + "               FIXED,\n"
                + "               SALESDOCUMENTID,\n" + "               FFPAGETYPE\n" + "            )\n"
                + "            Values\n" + "            (\n" + "             OId.get('FFContactLink'),\n"
                + "               0,\n" + "               vRecSdr.ffAddressType,\n" + "               null,\n"
                + "               ffCompanyIdForCL,\n" + "               ffTravellerPersonId,\n"
                + "               ffAddressId, -- toAddress\n" + "               ffFaxEmailId, -- toFax\n"
                + "               0, -- OrderNumber\n" + "               vRecSd.sdShippingSetting, --\n"
                + "               decode(vRecSd.sdShippingSetting,1,1,0), -- suppressed,\n"
                + "               decode(vRecSd.boState,0,0,\n" + "                                     1,0,\n"
                + "                                     2,0,\n" + "                                     3,0,\n"
                + "                                       1 ),  -- fixed = 1 if bostate >= 4\n"
                + "               vRecSdr.salesDocumentId,\n" + "               2 --Invoice\n" + "            );\n" +
                "\n"
                + "         END LOOP; -- cSdr\n" + "\n" + "      IF vRecSd.emailAddressForOriginal IS NOT NULL THEN\n"
                + "         ffFaxEmailId := OId.get('FFFaxEmail');\n" + "\n" + "         insert into FFFAXEMAIL\n"
                + "         (\n" + "            OBJECTIDENTIFIER,\n" + "            OBJECTVERSION,\n" +
                "            VALUE,\n"
                + "            RECEIVERNAME,\n" + "            ORIGINID,\n" + "            FAX,\n"
                + "            EMAILADDRESSESFORCOPY\n" + "         )\n" + "         Values\n" + "         (\n"
                + "            ffFaxEmailId,\n" + "            0,\n" + "            vRecSd.EmailAddressForOriginal,\n"
                + "            null, -- TODO what to set here? vRecSd.receivername,\n" + "            null,\n"
                + "            0,\n" + "            vRecSd.EmailAddressesForCopy\n" + "         );\n" + "\n"
                + "         insert into FFContactLink\n" + "         (\n" + "            OBJECTIDENTIFIER,\n"
                + "            OBJECTVERSION,\n" + "            FFADDRESSTYPE,\n" + "            FROMTRIPID,\n"
                + "            FROMCOMPANYID,\n" + "            FROMTRAVELLERID,\n" + "            TOADDRESSID,\n"
                + "            TOFAXEMAILID,\n" + "            ORDERNUMBER,\n" + "            FFSHIPPINGTYPE,\n"
                + "            SUPPRESSED,\n" + "            FIXED,\n" + "            SALESDOCUMENTID,\n"
                + "            FFPAGETYPE\n" + "         )\n" + "         Values\n" + "         (\n"
                + "            OId.get('FFContactLink'),\n" + "            0,\n" + "            1,   -- legalinvoice\n"
                + "            null,\n" + "            ffCompanyId,\n" + "            null,\n"
                + "            null, -- toAddress\n" + "            ffFaxEmailId, -- toFax\n"
                + "            0, -- OrderNumber\n" + "            vRecSd.sdShippingSetting, --\n"
                + "            decode(vRecSd.sdShippingSetting,1,1,0), --  suppressed,\n"
                + "            decode(vRecSd.boState,0,0,\n" + "                                  1,0,\n"
                + "                                  2,0,\n" + "                                  3,0,\n"
                + "                                    1 ),  -- fixed = 1 if bostate >= 4\n"
                + "            vRecSd.objectidentifier,\n" + "            2 --Invoice\n" + "         );\n" + "\n"
                + "      END IF;\n" + "\n" + "   END LOOP; -- cSd\n" + "END;\n" + "/";
        parser.iterateSQL(visitor, theSQL);
        assertEquals(1, visitor.getStatements().size());
    }

    public void testDropProcedure() throws SQLException, IOException {
        String theSQL = "drop procedure MigrationFF;";
        parser.iterateSQL(visitor, theSQL);
        assertEquals(1, visitor.getStatements().size());
    }

    public void testFixLF() {
        String fixed = parser.fixLF("/**\r\ntest\r\n**/BEGIN\nEND;\r\n\r\n/\r\n \r\n");
        assertEquals("/**\ntest\n**/BEGIN\nEND;", fixed);
    }
}
