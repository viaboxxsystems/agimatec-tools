import java.util.*;
import com.agimatec.annotations.jam.*;
import com.agimatec.annotations.*;

def dtoClasses = new HashSet();
dtoClasses.addAll(classes.findAll { it.getAnnotation(DTO.class) != null });
dtoClasses.addAll(classes.findAll { it.getAnnotation(DTOs.class) != null });
println "******** Found ${dtoClasses.size()} annotated classes out of ${classes.size()}"

generator = new JAMDtoGenerator();
generator.setTemplateDir("../annomark/templates");

generator.addInstruction("java-pojo", "target/generated/src/main/java", null)
    .setUsageQualifier("Edit")
    .setPrefix("Transfer")
    .setSuffix(".java")
    .setDefaultPackage("com.agimatec.annomark.example.transfer");
    
generator.addInstruction("dozer-mapping", "target/generated/src/main/resources", "dozerMapping-generated.xml")
    .setUsageQualifier("Edit")
    .setPrefix("Transfer")
    .setSuffix(".xml")
    .setDefaultPackage("com.agimatec.annomark.example.transfer");

generator.addInstruction("bean-infos-xml", "target/generated/src/main/resources", "beanInfos-generated.xml")
    .setUsageQualifier("Edit")
    .setPrefix("Transfer")
    .setDefaultPackage("com.agimatec.annomark.example.transfer");

generator.addInstruction("java-pojo", "target/generated/src/main/java", null)
    .setUsageQualifier("View")
    .setPrefix("Transfer")
    .setSuffix("Light.java")
    .setDefaultPackage("com.agimatec.annomark.example.transfer");

generator.addInstruction("dozer-mapping", "target/generated/src/main/resources", "dozerMappingLight-generated.xml")
    .setUsageQualifier("View")
    .setPrefix("Transfer")
    .setSuffix("Light.java")
    .setDefaultPackage("com.agimatec.annomark.example.transfer");

generator.generate(dtoClasses);
