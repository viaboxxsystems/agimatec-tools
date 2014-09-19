-- Oracle PL/SQL Package
CREATE OR REPLACE PACKAGE h_session
IS
   FUNCTION getContextId RETURN VARCHAR;
   PROCEDURE setContextId ( pCId IN VARCHAR);
END h_session;
/
