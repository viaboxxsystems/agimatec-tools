package com.agimatec.annotations.jam;

import com.agimatec.annotations.DTO;
import com.agimatec.annotations.DTOs;
import org.codehaus.jam.JAnnotatedElement;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JField;
import org.codehaus.jam.JMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 08.06.2007 <br/>
 * Time: 09:16:15 <br/>
 * Copyright: Apache 2.0 License
 */
public class JAMDtoClass extends JAMDtoAnnotatedElement {
    private List<JAMDtoField> fields;

    private final JClass mclass;

    public JAMDtoClass(JClass mclass) {
        this.mclass = mclass;
    }

    public boolean isEnumType() {
        return mclass.isEnumType();
    }

    /** @return null for default, otherwise the explicitly given name from the annotation */
    public String getDtoClassName() {
        JAMAnnotation ja = getDtoAnnotation();
        JAMGenInstruction instruct = JAMDtoGenerator.getCurrentInstruction();
        return stringValue(ja == null ? null : ja.getStringValue("dtoClass"),
              trimEnding(instruct.getPrefix() + getSimpleName() + instruct.getSuffix()));
    }

    /** find first annotated element with method annotation of given type */
    public JAMDtoFieldAnnotation findByGetterAnnotation(String annotationName) {
        for (JAMDtoFieldAnnotation each : getDtoFieldAnnotations()) {
            JAMDtoMethod m = getMethod(each.getGetterName());
            if (m != null) {
                JAMAnnotation a = m.getAnnotation(annotationName);
                if (a != null) return each;
            }
        }
        return null;
    }

    /**
     * find first annotated element (field or method) that has an annotation
     * of the given type
     */
    public JAMDtoFieldAnnotation findByElementAnnotation(String annotationName) {
        for (JAMDtoFieldAnnotation each : getDtoFieldAnnotations()) {
            JAMAnnotation a = each.getElement().getAnnotation(annotationName);
            if (a != null) return each;
        }
        return null;
    }

    private String trimEnding(String fileName) {
        int li = fileName.lastIndexOf('.');
        if (li > -1) {
            return fileName.substring(0, li);
        } else {
            return fileName;
        }
    }

    public List<JAMDtoField> getDtoField() {
        List<JAMDtoField> dtoFields = new ArrayList(getFields().size());

        for (JAMDtoField field : getFields()) {
            JAMAnnotation anno = field.getDtoAnnotation();
            if (anno != null) dtoFields.add(field);
        }
        return dtoFields;
    }

    public JAMDtoMethod getMethod(String methodName) {
        for (JMethod method : mclass.getMethods()) {
            if (method.getSimpleName().equals(methodName)) {
                return new JAMDtoMethod(method, this);
            }
        }
        return null;
    }

    public List<JAMDtoFieldAnnotation> getDtoFieldAnnotations() {
        List<JAMDtoFieldAnnotation> dtoFields = new ArrayList(getFields().size() + 10);

        for (JAMDtoField field : getFields()) {
            JAMAnnotation[] annos = field.getDtoAnnotations();
            if (annos != null) {
                for (JAMAnnotation anno : annos) {
                    dtoFields.add(new JAMDtoFieldAnnotation(anno, field));
                }
            }
        }

        for (JMethod method : mclass.getMethods()) {
            JAMDtoMethod dtomethod = new JAMDtoMethod(method, this);
            JAMAnnotation[] annos = dtomethod.getDtoAnnotations();
            if (annos != null) {
                for (JAMAnnotation anno : annos) {
                    dtoFields.add(new JAMDtoFieldAnnotation(anno, dtomethod));
                }
            }
        }
        return dtoFields;
    }

    public List<JAMDtoField> getFields() {
        if (fields == null) {
            fields = new ArrayList(mclass.getFields().length);
            for (JField each : mclass.getFields()) {
                fields.add(new JAMDtoField(each, this));
            }
        }
        return fields;
    }

    public String getSimpleName() {
        return mclass.getSimpleName();
    }

    public String getName() {
        return mclass.getQualifiedName();
    }

    public String getType() {
        return getName();
    }

    public JClass getTypeJClass() {
        return mclass;
    }

    public JAMDtoClass getDtoClass() {
        return this;
    }

    public String getPackageName() {
        return mclass.getContainingPackage().getQualifiedName();
    }

    public String getDtoPackageName() {
        String pn = JAMDtoGenerator.getCurrentInstruction().getDefaultPackage();
        if (pn == null || pn.length() == 0) {
            pn = getPackageName();
        }
        JAMAnnotation ja = getDtoAnnotation();
        if (ja == null) {
            return pn;
        }
        return stringValue(ja.getStringValue("dtoPackage"), pn);
    }

    public String getDtoPackagePath() {
        return getDtoPackageName().replace('.', '/');
    }

    public JAnnotatedElement element() {
        return mclass;
    }

    protected String singleAnnotation() {
        return DTO.class.getName();
    }

    protected String multiAnnotation() {
        return DTOs.class.getName();
    }

    public JField getTypeField(String path) {
        if (path == null || path.length() == 0) return null;
        StringTokenizer tokens = new StringTokenizer(path, ".");
        JField current = null;
        do {
            String each = tokens.nextToken();
            current = findField(current, each);
        } while (tokens.hasMoreTokens() && current != null);
        if (current == null) return null;
        else return current;
    }

    public JAMDtoMethod findMethod(String methodName) {

        for (JMethod method : mclass.getMethods()) {
            if (method.getSimpleName().equals(methodName)) {
                return new JAMDtoMethod(method, this);
            }
        }
        return null;
    }
}
