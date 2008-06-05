agimatec-tools by agimatec GmbH
=======================================
The project agimatec-tools is a bundle that contains some frameworks used for:
- file generation (source code, configuration)
- database migration (with sql- and groovy-scripts, xml-configuration, schema sanity check etc.)
- database import (csv-, fixedlength-, xml-format)

How to compile the project
==========================
Requirements:
0. Sources require java1.5 or higher. (Tested with JDK 1.5.0._12)
1. The project is built with maven2. You need to download and install maven2 from: http://maven.apache.org/
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
http://www.agimatec.de
http://www.agimatec.de/blog

Roman Stumm, agimatec GmbH, 2008
email: roman.stumm@agimatec.de
