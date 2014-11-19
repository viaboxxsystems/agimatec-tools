package com.agimatec.annotations.jam;

import com.agimatec.annotations.DTOAttribute;
import com.agimatec.annotations.DTOAttributes;
import com.sun.javadoc.Type;
import com.sun.tools.javadoc.FieldDocImpl;
import com.sun.tools.javadoc.ParameterizedTypeImpl;
import org.codehaus.jam.JAnnotatedElement;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 08.06.2007 <br/>
 * Time: 12:07:58 <br/>
 * Copyright: Apache 2.0 License
 */
public class JAMDtoField extends JAMDtoAnnotatedElement {
    private static final Logger log = LoggerFactory.getLogger(JAMDtoField.class);
    private final JField field;
    private final JAMDtoClass dtoClass;

    public JAMDtoField(JField field, JAMDtoClass dtoClass) {
        this.field = field;
        this.dtoClass = dtoClass;
        if (field == null) {
            //noinspection ThrowableInstanceNeverThrown
            log.error("no underlying field found: " + this, new NullPointerException());
        }
    }

    public JAMDtoClass getDtoClass() {
        return dtoClass;
    }

    public JAnnotatedElement element() {
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

    public JField getTypeField(String path) {
        if (path == null || path.length() == 0) return field;
        StringTokenizer tokens = new StringTokenizer(path, ".");
        JField current = field;
        while (tokens.hasMoreTokens() && current != null) {
            String each = tokens.nextToken();
            current = findField(current, each);
        }
        return current;
    }

    @Override
    public String getGenericParameter() {
        return getGenericParameter(field);
    }

    public static String getGenericParameter(JField aField) {
        Type genericType = null;
        // hack: access type of element for generic collections
        if(aField == null) return null;
        Object artifact = aField.getArtifact();
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
