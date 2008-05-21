package com.agimatec.annotations.jam;

import com.agimatec.annotations.DTOAttribute;
import com.agimatec.annotations.DTOAttributes;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jam.JAnnotatedElement;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JField;
import org.codehaus.jam.JMethod;

import java.util.StringTokenizer;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 13.06.2007 <br/>
 * Time: 14:55:23 <br/>
 * Copyright: Agimatec GmbH
 */
public class JAMDtoMethod extends JAMDtoAnnotatedElement {
    private final JMethod jmethod;
    private final JAMDtoClass dtoClass;

    public JAMDtoMethod(JMethod jmethod, JAMDtoClass dtoClass) {
        this.jmethod = jmethod;
        this.dtoClass = dtoClass;
    }

    public JAMDtoClass getDtoClass() {
        return dtoClass;
    }


    protected JAnnotatedElement element() {
        return jmethod;
    }

    protected String singleAnnotation() {
        return DTOAttribute.class.getName();
    }

    protected String multiAnnotation() {
        return DTOAttributes.class.getName();
    }

    public String getName() {
        return StringUtils.uncapitalize(jmethod.getSimpleName().substring(3));
    }

    public String getType() {
        return jmethod.getReturnType().getQualifiedName();
    }

    public JClass getTypeJClass() {
        return jmethod.getReturnType();
    }

    public String getType(String path) {
        if (path == null || path.length() == 0) return getType();
        StringTokenizer tokens = new StringTokenizer(path, ".");
        JClass current = jmethod.getReturnType();
        while (tokens.hasMoreTokens() && current != null) {
            String each = tokens.nextToken();
            JField field = findField(current, each);
            current = (field == null) ? null : field.getType();
        }
        if (current == null) return null;
        else return current.getQualifiedName();
    }

    public boolean isEnumType() {
        return jmethod.getReturnType().isEnumType();
    }
}
