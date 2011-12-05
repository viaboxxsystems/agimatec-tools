package com.agimatec.annotations.jam;

import com.agimatec.annotations.DTO;
import com.agimatec.annotations.DTOs;
import com.agimatec.annotations.TestDocumentation;
import com.agimatec.annotations.TestMethodDocumentation;
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
 * Copyright: Agimatec GmbH
 */
public class JAMTestClass extends JAMTestAnnotatedElement {

    private final JClass mclass;

    public JAMTestClass(JClass mclass) {
        this.mclass = mclass;
    }

    /** @return null for default, otherwise the explicitly given name from the annotation */
    public String getTestClassName() {
        JAMAnnotation ja = getTestAnnotation();
        JAMGenInstruction instruct = JAMTestGenerator.getCurrentInstruction();
        return stringValue(ja == null ? null : ja.getStringValue("testClass"),
                trimEnding(instruct.getPrefix() + getSimpleName() + instruct.getSuffix()));
    }


    private String trimEnding(String fileName) {
        int li = fileName.lastIndexOf('.');
        if (li > -1) {
            return fileName.substring(0, li);
        } else {
            return fileName;
        }
    }


    public List<JAMTestMethod> getAnnotatedMethods() {
        List<JAMTestMethod> methods=new ArrayList();
        for (JMethod method : mclass.getMethods()) {
            if (method.getAnnotation(TestMethodDocumentation.class)!=null)
                methods.add(new JAMTestMethod(method, this));

        }

        return methods;
    }

    public JAMTestMethod getMethod(String methodName) {
        for (JMethod method : mclass.getMethods()) {
            if (method.getSimpleName().equals(methodName)) {
                return new JAMTestMethod(method, this);
            }
        }
        return null;
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

    public JAMTestClass getTestClass() {
        return this;
    }

    public String getPackageName() {
        return mclass.getContainingPackage().getQualifiedName();
    }

    public String getTestPackageName() {
        String pn = JAMTestGenerator.getCurrentInstruction().getDefaultPackage();
        if (pn == null || pn.length() == 0) {
            pn = getPackageName();
        }
        JAMAnnotation ja = getTestAnnotation();
        if (ja == null) {
            return pn;
        }
        return stringValue(ja.getStringValue("testPackage"), pn);
    }

    public String getTestPackagePath() {
        return getTestPackageName().replace('.', '/');
    }

    protected JAnnotatedElement element() {
        return mclass;
    }

    protected String singleAnnotation() {
        return TestDocumentation.class.getName();
    }

    protected String multiAnnotation() {
        return TestMethodDocumentation.class.getName();
    }

    public String getType(String path) {
        if (path == null || path.length() == 0) return getType();
        StringTokenizer tokens = new StringTokenizer(path, ".");
        JField current = null;
        do {
            String each = tokens.nextToken();
            current = findField(current, each);
        } while (tokens.hasMoreTokens() && current != null);
        if (current == null) return null;
        else return current.getType().getQualifiedName();
    }
}