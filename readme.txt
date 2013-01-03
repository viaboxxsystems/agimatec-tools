agimatec-tools
==============
The project agimatec-tools is a bundle that contains some frameworks used for:
- file generation (source code, configuration)
- database migration (with sql- and groovy-scripts, xml-configuration, schema sanity check etc.)
- database import (csv-, fixedlength-, xml-format)

History
-------
1. first open-source release by Roman Stumm, agimatec GmbH, Bonn
2. currently maintained by Roman Stumm, Viaboxx GmbH, K�nigswinter

How to compile the project
==========================
Requirements:
0. Sources require java1.5 or higher. (Tested with JDK 1.6.0_24)
1. Maven2 required
1a. The project is built with maven2.0.9. 
You need to download and install maven2 from: http://maven.apache.org/

1b. Prepare local maven2 repository:
--------------------------------
Some dependencies might not be available from official maven2-repositories:
  * Dependency: com.sun:tools:jar:1.5.0_12 missing?

  Using a different JDK than 1.5.0_12?
  ------------------------------------
  Change      
      <dependency>
        <groupId>com.sun</groupId>
        <artifactId>tools</artifactId>
        <version>1.5.0_12</version>
        <scope>system</scope>
        <systemPath>${java.home}/../lib/tools.jar</systemPath>      
      </dependency>
  in agimatec-tools/pom.xml to match the JDK you are using.
  
  * Dependency: oracle:ojdbc14:10.2.0.4 missing?
  You can download the oracle driver from oracle sites, e.g. 
  http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/index.html
  Change the version accordingly in agimatec-tools/pom.xml.
  Copy the jar to $LOCAL_REPOSITORY/oracle/ojdbc14/<oracle-version>/ojdbc14-<oracle-version>.jar  

  * Dependency: annogen:annogen:0.1.1 missing?
  Download annogen-0.1.1.zip from this side and unzip it.
  Copy annogen-0.1.1.jar to $LOCAL_REPOSITORY/annogen/annogen/0.1.1/annogen-0.1.1.jar
        
2. Invoke maven2 from within one of the directories that contain a pom.xml file

compile project:
----------------
mvn install

(artifacts are generated into the target directories)

(optional) generate site, javadoc:
-----------------------
mvn site

(optional) generate an IntelliJ project:
-----------------------------
mvn idea:idea

(optional) deploy maven-site and javadoc:
------------------------------
[ Note:
Follow the instructions on
 * https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide
 * https://oss.sonatype.org/index.html#welcome
to deploy on Sonatype OSS Maven Repository.

Old style (when distribution-management in pom.xml is enabled)
  You must set the properties ${agimatec-site-id} and ${agimatec-site-url} to
  adequate values. You can do that by adding them to your maven settings.xml. This is the place
  where the server credenticals for uploads are kept. ]
 
mvn site-deploy

Getting started
---------------
Refer to the project page and WIKI at:
http://code.google.com/p/agimatec-tools/

You can checkout latest sources and releases from there.
You can also refer to the test cases, examples and templates.

Feedback, questions, contribution
=================================
** Your feedback is welcome! **

http://code.google.com/p/agimatec-tools/
http://www.viaboxxsystems.de
http://www.viaboxxsystems.de/blog

Roman Stumm, Viaboxx GmbH, 2008-2013
email: roman.stumm@viaboxx.de
