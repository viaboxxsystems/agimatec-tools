package com.agimatec.annotations.jam;

import com.agimatec.annotations.DTOAttribute;
import com.agimatec.annotations.DTOAttributes;
import com.sun.javadoc.Type;
import com.sun.tools.javadoc.FieldDocImpl;
import com.sun.tools.javadoc.ParameterizedTypeImpl;
import org.codehaus.jam.JAnnotatedElement;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JField;

import java.util.StringTokenizer;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 08.06.2007 <br/>
 * Time: 12:07:58 <br/>
 * Copyright: Agimatec GmbH
 */
public class JAMDtoField extends JAMDtoAnnotatedElement {
    private final JField field;
    private final JAMDtoClass dtoClass;

    public JAMDtoField(JField field, JAMDtoClass dtoClass) {
        this.field = field;
        this.dtoClass = dtoClass;
    }

    public JAMDtoClass getDtoClass() {
        return dtoClass;
    }

    protected JAnnotatedElement element() {
        return field;
    }

    protected String singleAnnotation() {
        return DTOAttribute.class.getName();
    }

    protected String multiAnnotation() {
        return DTOAttributes.class.getName();
    }

    public boolean isEnumType() {
        return field.getType().isEnumType();
    }

    public String getName() {
        return field.getSimpleName();
    }

    public String getType() {
        return field.getType().getQualifiedName();
    }

    public JClass getTypeJClass() {
        return field.getType();
    }

    public String getType(String path) {
        if (path == null || path.length() == 0) return getType();
        StringTokenizer tokens = new StringTokenizer(path, ".");
        JField current = field;
        while (tokens.hasMoreTokens() && current != null) {
            String each = tokens.nextToken();
            current = findField(current, each);
        }
        if (current == null) return null;
        else return current.getType().getQualifiedName();
    }

    @Override
    public String getGenericParameter() {
        Type genericType = null;
        // hack: access type of element for generic collections
        Object artifact = field.getArtifact();
        if (artifact instanceof FieldDocImpl) {
            Type type = ((FieldDocImpl) artifact).type();
            if (type instanceof ParameterizedTypeImpl) {
                Type[] args = ((ParameterizedTypeImpl) type).typeArguments();
                if (args != null && args.length == 1) {
                    genericType = args[0];
                }
            }
        }
        return genericType == null ? null : genericType.toString();
    }

}
