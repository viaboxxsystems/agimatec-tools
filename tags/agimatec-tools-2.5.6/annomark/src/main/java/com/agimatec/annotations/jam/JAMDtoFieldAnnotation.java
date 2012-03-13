package com.agimatec.annotations.jam;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jam.JField;
import org.codehaus.jam.JMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 08.06.2007 <br/>
 * Time: 17:18:39 <br/>
 * Copyright: Agimatec GmbH
 */
public class JAMDtoFieldAnnotation {
    private final JAMAnnotation annotation;
    private final JAMDtoAnnotatedElement element;
    private JAMDtoAnnotatedElement[] targetElements;

    public JAMDtoFieldAnnotation(JAMAnnotation annotation, JAMDtoField field) {
        this.annotation = annotation;
        this.element = field;
    }

    public JAMDtoFieldAnnotation(JAMAnnotation annotation, JAMDtoMethod method) {
        this.annotation = annotation;
        this.element = method;
    }

    public JAMAnnotation getAnnotation() {
        return annotation;
    }

    public JAMDtoField getField() {
        return (JAMDtoField) (element instanceof JAMDtoField ? element : null);
    }

    public JAMDtoAnnotatedElement getElement() {
        return element;
    }

    public boolean isDtoCopyByReference() {
        return annotation.getBooleanValue("copyByReference");
    }

    public boolean isDtoOneWay() {
        return annotation.getBooleanValue("oneWay");
    }

    public String getDtoConverter() {
        return getDtoAnnoValue("converter");
    }

    public String getDtoPath() {
        return getDtoAnnoValue("path");
    }

    public String getDtoAddMethod() {
        return getDtoAnnoValue("addMethod");
    }

    private String getDtoAnnoValue(String annoAttribute) {
        String val = annotation.getStringValue(annoAttribute);
        return val == null || val.length() == 0 ? null : val;
    }

    public String getDtoName() {
        String name = annotation.getStringValue("property");
        return name.length() == 0 ? element.getName() : name;
    }

    public String getDtoType() {
        String type = annotation.getStringValue("type");
        return type == null || type.length() == 0 ? null : type;
    }

    public JAMAnnotation findGetterAnnotation(String annotationName) {
        if (getElement() instanceof JAMDtoMethod) {
            return getElement().getAnnotation(annotationName);
        } else {
            JAMDtoMethod method = getElement().getDtoClass().findMethod(getGetterName());
            if (method != null) {
                return method.getAnnotation(annotationName);
            }
        }
        return null;
    }

    /** der element-type des dto-targets, d.h. bei listen der generic-type */
    public String getDtoFieldType() {
        final String type;

        String dtotype = getDtoType();
        if (dtotype == null) {
            final String dt, gp;
            if (getDtoPath() == null) {
                dt = element.getType();
                gp = getElement().getGenericParameter();
            } else {
                JField field = element.getTypeField(getDtoPath());
                if (field == null) { // try method
                    if (element.element() instanceof JMethod) {
                        gp = JAMDtoMethod.getGenericParameter((JMethod) element.element());
                    } else {
                        gp = null;
                    }
                    dt = element.getType();
                } else {
                    gp = JAMDtoField.getGenericParameter(field);
                    dt = field.getType().getQualifiedName();
                }
            }
            type = toDtoType(dt, gp);
        } else if (dtotype.indexOf("<") > 0) {
            int i1 = dtotype.indexOf("<");
            return dtotype.substring(0, i1 + 1) + optimizeType(dtotype.substring(i1 + 1));
        } else {
            type = dtotype;
        }
        return optimizeType(type);
    }

    /**
     * @param dt - data type
     * @param gp - generic parameter or null
     * @return type in dto for the given type and generic parameter
     */
    private String toDtoType(String dt, String gp) {
        String type;
        if (gp != null) {
            JAMDtoClass jc = JAMDtoGenerator.getJAMClass(gp);
            String cn;
            if (jc != null) {
                cn = optimizeType(
                      jc.getDtoPackageName() + "." + jc.getDtoClassName());
            } else {
                cn = optimizeType(gp);
            }
            type = dt + "<" + cn + ">";
        } else {
            JAMDtoClass jc = JAMDtoGenerator.getJAMClass(dt);
            if (jc != null) {
                type = jc.getDtoPackageName() + "." + jc.getDtoClassName();
            } else {
                type = dt;
            }
        }
        return type;
    }

    /**
     * @return the type of the dto bean (used for relationship beanId="dtoBeanType")
     */
    public String getDtoBeanType() {
        final String type;

        String dtotype = getDtoType();
        if (dtotype == null) {
            String gp = getTargetElement().getGenericParameter();
            if (gp != null) {
                JAMDtoClass jc = JAMDtoGenerator.getJAMClass(gp);
                String cn;
                if (jc != null) {
                    cn = jc.getDtoPackageName() + "." + jc.getDtoClassName();
                } else {
                    cn = gp;
                }
                type = cn;
            } else {
                String dt = getTargetElement().getType();
                JAMDtoClass jc = JAMDtoGenerator.getJAMClass(dt);
                if (jc != null) {
                    type = jc.getDtoPackageName() + "." + jc.getDtoClassName();
                } else {
                    type = dt;
                }
            }
        } else if (dtotype.indexOf('<') > 0) {
            return extractGenericParameter(dtotype);
        } else {
            type = dtotype;
        }
        return type;
    }

    private String extractGenericParameter(String dtotype) {
        int i1 = dtotype.indexOf('<');
        int i2 = dtotype.lastIndexOf('>');
        if (i2 > i1) {
            return dtotype.substring(i1 + 1, i2);
        } else { // ???!!
            return dtotype.substring(i1 + 1);
        }
    }

    public String getHintType() {
        String gp = getTargetElement() != null ?
              getTargetElement().getGenericParameter() : null;
        if (gp != null) {
            return gp;
        }
        return null;
    }

    private String optimizeType(String type) {
        String myPackage = element.getDtoClass().getDtoPackageName() + ".";
        if (type == null) return type;
        if (type.lastIndexOf('.') == "java.lang".length() &&
              type.startsWith("java.lang.")) {
            return type.substring("java.lang.".length());
        } else
        if (type.lastIndexOf('.') == (myPackage.length()-1) && type.startsWith(myPackage)) {
            return type.substring(myPackage.length());
        } else {
            return type;
        }
    }

    public String getGetterName() {
        return getGetterName(getElement().getName(), getDtoFieldType());
    }

    public static String getGetterName(String name, String type) {
        if (type.equals("boolean")) {
            return "is" + StringUtils.capitalize(name);
        } else {
            return "get" + StringUtils.capitalize(name);
        }
    }

    /** @return true wenn mind. ein element im pfad nullable ist */
    public boolean isNullable() {
        JAMDtoAnnotatedElement[] elements = getTargetElements();
        for (JAMDtoAnnotatedElement each : elements) {
            String getter = getGetterName(each.getName(), each.getType());
            JAMDtoMethod meth = each.getDtoClass().getMethod(getter);
            if (meth == null) return true;
            JAMAnnotation anno = meth.getAnnotation("javax.persistence.Column");
            if (anno == null) {
                anno = meth.getAnnotation("javax.persistence.JoinColumn");
            }
            if (anno != null) {
                if (anno.getBooleanValue("nullable")) return true;
            } else {
                return true;
            }
        }
        return false;
    }

    /** @return true wenn alle elemente im pfad unique sind */
    public boolean isUnique() {
        JAMDtoAnnotatedElement[] elements = getTargetElements();
        for (JAMDtoAnnotatedElement each : elements) {
            String getter = getGetterName(each.getName(), each.getType());
            JAMDtoMethod meth = each.getDtoClass().getMethod(getter);
            if (meth == null) return false;
            JAMAnnotation anno = meth.getAnnotation("javax.persistence.Column");
            if (anno == null) {
                anno = meth.getAnnotation("javax.persistence.JoinColumn");
            }
            if (anno != null) {
                if (!anno.getBooleanValue("unique")) return false;
            } else {
                return false;
            }
        }
        return true;
    }

    /** =length des letzten elements im pfad */
    public Integer getLength() {
        JAMDtoAnnotatedElement field = getTargetElement();
        if (field != null) {
            String getter = getGetterName(field.getName(), field.getType());
            JAMDtoMethod meth = field.getDtoClass().getMethod(getter);
            if (meth == null) return null;
            JAMAnnotation anno = meth.getAnnotation("javax.persistence.Column");
            if (anno != null) {
                return anno.getIntValue("length");
            }
        }
        return null;
    }

    /** das letzte element im pfad */
    protected JAMDtoAnnotatedElement getTargetElement() {
        JAMDtoAnnotatedElement[] elements = getTargetElements();
        return elements[elements.length - 1];
    }

    protected JAMDtoAnnotatedElement[] getTargetElements() {
        if (targetElements != null) return targetElements;
        List<JAMDtoAnnotatedElement> elements = new ArrayList(2);
        elements.add(element);
        JAMDtoAnnotatedElement field = element;
        String path = getDtoPath();
        while (path != null) {
            int idx = path.indexOf('.');
            if (idx < 0) {
                field = new JAMDtoField(
                      JAMDtoAnnotatedElement.findField(field.getTypeJClass(), path),
                      new JAMDtoClass(field.getTypeJClass()));
                elements.add(field);
                path = null;
            } else {
                String next = path.substring(0, idx);
                JField jf = JAMDtoAnnotatedElement
                      .findField(field.getTypeJClass(), next);
                if (jf != null) {
                    field = new JAMDtoField(jf, field.getDtoClass());
                    elements.add(field);
                }
                path = path.substring(next.length()+1);
            }
        }
        targetElements = elements.toArray(new JAMDtoAnnotatedElement[elements.size()]);
        return targetElements;
    }

    public boolean isRelationship() {
        JAMDtoMethod meth;
        if (getDtoPath() != null) {
            String aName = getDtoPath().substring(getDtoPath().lastIndexOf('.') + 1);
            meth = getTargetElement().getDtoClass()
                  .getMethod(getGetterName(aName, getTargetElement().getType()));
        } else {
            meth = getElement().getDtoClass().getMethod(getGetterName());
        }
        return meth != null && getDtoConverter() == null && (
              null != meth.getAnnotation("javax.persistence.OneToMany") ||
                    null != meth.getAnnotation("javax.persistence.ManyToOne") ||
                    null != meth.getAnnotation("javax.persistence.ManyToMany") ||
                    null != meth.getAnnotation("javax.persistence.OneToOne"));
    }

    public String toString() {
        return element + ";" + annotation;
    }
}
