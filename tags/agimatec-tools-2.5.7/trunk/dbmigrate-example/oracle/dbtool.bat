rem This script can be used to execute setup scripts
rem for the database with the dbmigrate java program
rem Use with caution!
rem $Id: $
rem $Author: roman.stumm $
rem ---- Usage: dbtool.bat [setup.xml|upgrade.xml] ----

set JAVA_BIN=java

SET CLASSPATH=lib\ojdbc14.jar;lib\log4j.jar;lib\dbunit.jar;lib\dbmigrate-example.jar;lib\dbmigrate.jar
SET CLASSPATH=%CLASSPATH%;lib\groovy-all-1.0-jsr.jar;lib\dbimport.jar;lib\freemarker.jar;lib\commons-io.jar;lib\commons-logging.jar;lib\commons-lang.jar

%JAVA_BIN% -cp %CLASSPATH% -Dfile.encoding=UTF-8 com.agimatec.dbmigrate.AutoMigrationTool -conf %1%
