-- Recompiliert alle Packages, Function, Trigger und Procedures auf der DB
-- empfehlenswert nach einer Migration, da dann solche DB-Objekte invalid sein können
-- Date         Author  Description
-- 26.11.2004   RSt     2nd Loop for avoid invalid triggers

BEGIN
     FOR ONE IN (select object_type, object_name 
                 from user_objects 
                 where object_type in ('FUNCTION','PACKAGE','PACKAGE BODY',
                                       'PROCEDURE')) LOOP
        dbms_ddl.alter_compile(one.object_type, NULL, one.object_name);
     END LOOP;

     FOR ONE IN (select object_type, object_name 
                 from user_objects 
                 where object_type in ('TRIGGER')) LOOP
        dbms_ddl.alter_compile(one.object_type, NULL, one.object_name);
     END LOOP;
END;
/
