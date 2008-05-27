CREATE OR REPLACE PACKAGE BODY h_session
IS
lContextId VARCHAR(40);

FUNCTION getContextId RETURN VARCHAR IS
   BEGIN
       RETURN lContextId;
   END getContextId;

PROCEDURE setContextId ( pCId IN VARCHAR) IS
   BEGIN
       lContextId := pCId;
   END setContextId;

END h_session;
/