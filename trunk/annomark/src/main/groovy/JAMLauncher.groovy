import java.util.*;
import com.agimatec.annotations.jam.*;
import com.agimatec.annotations.*;

def dtoClasses = new HashSet();
dtoClasses.addAll(classes.findAll { it.getAnnotation(DTO.class) != null });
dtoClasses.addAll(classes.findAll { it.getAnnotation(DTOs.class) != null });
println "Found ${dtoClasses.size()} annotated classes out of ${classes.size()}"

generator = new JAMDtoGenerator();
generator.setTemplateDir("templates");
generator.addInstruction("java-pojo", "target/pojo", null)
    .setUsageQualifier("XFire")
    .setPrefix("XFire")
    .setSuffix(".java")
    .setDefaultPackage("com.agimatec.connecta.xfire.model");
generator.addInstruction("java-pojo", "target/adm", null)
    .setUsageQualifier("ADM")
    .setPrefix("ADM")
    .setSuffix(".java")
    .setDefaultPackage("com.agimatec.nucleus.xml.model");
generator.addInstruction("dozer-mapping", "target/mapping", "xfireBeanMappings.xml")
    .setUsageQualifier("XFire")
    .setSuffix(".xml")
    .setDefaultPackage("com.agimatec.connecta.xfire.model");

generator.generate(dtoClasses);