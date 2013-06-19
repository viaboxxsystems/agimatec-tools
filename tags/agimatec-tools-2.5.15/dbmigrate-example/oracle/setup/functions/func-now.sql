-- Kompatiblitätsfunktionen zwischen Postgres und Oracle

-- create or replace TYPE BOOLEAN IS NUMBER(1); -- ist leider nicht universell verwendbar und versteht kein true/false

CREATE OR REPLACE FUNCTION now RETURN TIMESTAMP IS
BEGIN
  RETURN SYSDATE;
END now;
/
 
-- testen: SELECT now() from DUAL;
-- drop function now;

-- testen: select seq_next('hibernate_sequence') from dual;
-- heisst die Function jedoch nextval() kommt der Fehler: ORA-00904: "NEXTVAL": ungültiger Bezeichner

