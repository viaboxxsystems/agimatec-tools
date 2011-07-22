CREATE OR REPLACE FUNCTION seq_next(seqName IN VARCHAR2) RETURN NUMBER  IS
    nNextval NUMBER;
BEGIN
  EXECUTE IMMEDIATE 'begin SELECT ' || seqName || '.nextval INTO :a FROM DUAL; end;' USING OUT nNextval;
  RETURN nNextval;
END seq_next;
/