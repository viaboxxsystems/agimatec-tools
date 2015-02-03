/**********
 * Kompatiblitätsfunktion Oracle/Postgres.
 * Gibt zurück, ob ein Wert 'true' ist.
 * Vereinbarung: null is weder true noch false.
 *
 * Autor: Roman Stumm
 * Historie:
 * 09.05.08  Initial
 **********/
CREATE OR REPLACE FUNCTION bool2int
    (val IN NUMBER)
    RETURN NUMBER
IS
BEGIN
  IF val = 0 THEN
    return 0;
  ELSIF val IS NULL THEN
    return null;
  ELSE
    return 1;
  END IF;
END bool2int;
/
