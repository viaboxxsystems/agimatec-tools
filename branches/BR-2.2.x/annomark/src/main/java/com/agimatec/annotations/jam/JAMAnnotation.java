package com.agimatec.annotations.jam;

import org.codehaus.jam.JAnnotation;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 08.06.2007 <br/>
 * Time: 11:51:38 <br/>
 * Copyright: Agimatec GmbH
 */
public class JAMAnnotation {
    private final JAnnotation annotation;
    private JAMAnnotation[] annoarray;

    public JAMAnnotation(JAnnotation a) {
        annotation = a;
    }

    public String getStringValue(String name) {
        return annotation.getValue(name).asString();
    }

    public boolean getBooleanValue(String name) {
        return annotation.getValue(name).asBoolean();
    }

    public int getIntValue(String name) {
        return annotation.getValue(name).asInt();
    }

    public String toString() {
        return annotation.toString();
    }

    public JAMAnnotation[] getAnnotationArray() {
        if (annoarray == null) {
            JAnnotation[] array = annotation.getValues()[0].asAnnotationArray();
            annoarray = new JAMAnnotation[array.length];
            int i = 0;
            for (JAnnotation each : array) {
                annoarray[i++] = new JAMAnnotation(each);
            }
        }
        return annoarray;
    }

}
