package com.agimatec.annotations.jam;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jam.JAnnotatedElement;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JField;
import org.codehaus.jam.JMethod;

import java.util.StringTokenizer;

import com.agimatec.annotations.TestMethodDocumentation;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 13.06.2007 <br/>
 * Time: 14:55:23 <br/>
 * Copyright: Apache 2.0 License
 */
public class JAMTestMethod extends JAMTestAnnotatedElement {
    private final JMethod jmethod;
    private final JAMTestClass testClass;

    public JAMTestMethod(JMethod jmethod, JAMTestClass TestClass) {
        this.jmethod = jmethod;
        this.testClass = TestClass;
    }

    public JAMTestClass getTestClass() {
        return testClass;
    }


    protected JAnnotatedElement element() {
        return jmethod;
    }

    protected String singleAnnotation() {
        return TestMethodDocumentation.class.getName();
    }

    protected String multiAnnotation() {
        return TestMethodDocumentation.class.getName();
    }

    public String getName() {
        int offset = 3;
        if (getType().equals("boolean") && jmethod.getSimpleName().startsWith("is")) {
            offset = 2;
        }
        return StringUtils.uncapitalize(jmethod.getSimpleName().substring(offset));
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