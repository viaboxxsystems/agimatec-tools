# This script can be used to execute upgrade scripts
# for the database with the dbmigrate java program
# Use with caution!
# $Author: roman.stumm $
# ---- Usage: ./dbtool.sh [setup.xml|upgrade.xml] ----

JAVA_HOME=/usr/jdk/latest
JAVA_BIN=$JAVA_HOME/bin/java

CLASSPATH=lib/ojdbc14.jar:lib/log4j.jar:lib/dbunit.jar:lib/dbmigrate-example.jar:lib/dbmigrate.jar
CLASSPATH=$CLASSPATH:lib/groovy-all-1.0-jsr.jar:lib/dbimport.jar:lib/freemarker.jar:lib/commons-io.jar:;lib/slf4j-log4j.jar:lib/slf4j-api.jar:lib/commons-lang.jar
 
$JAVA_BIN -cp $CLASSPATH -Dfile.encoding=UTF-8 com.agimatec.dbmigrate.AutoMigrationTool -conf $1 
