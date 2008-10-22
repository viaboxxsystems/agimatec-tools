package com.agimatec.annotations.jam;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jam.JField;

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

    /** der element-type des dto-targets, d.h. bei listen der generic-type */
    public String getDtoFieldType() {
        final String type;

        String dtotype = getDtoType();
        if (dtotype == null) {
            String gp = getField() != null ? getField().getGenericParameter() : null;
            if (gp != null) {
                JAMDtoClass jc = JAMDtoGenerator.getJAMClass(gp);
                String cn;
                if (jc != null) {
                    cn = optimizeType(
                            jc.getDtoPackageName() + "." + jc.getDtoClassName());
                } else {
                    cn = optimizeType(gp);
                }
                type = element.getType() + "<" + cn + ">";
            } else {
                String dt = element.getType(getDtoPath());
                JAMDtoClass jc = JAMDtoGenerator.getJAMClass(dt);
                if (jc != null) {
                    type = jc.getDtoPackageName() + "." + jc.getDtoClassName();
                } else {
                    type = dt;
                }
            }
        } else if (dtotype.indexOf("<") > 0) {
            int i1 = dtotype.indexOf("<");
            return dtotype.substring(0, i1 + 1) + optimizeType(dtotype.substring(i1 + 1));
        } else {
            type = dtotype;
        }
        return optimizeType(type);
    }

    public String getDtoBeanType() {
        final String type;

        String dtotype = getDtoType();
        if (dtotype == null) {
            String gp = getField() != null ? getField().getGenericParameter() : null;
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
                String dt = element.getType(getDtoPath());
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
        if(i2 > i1) {
            return dtotype.substring(i1+1, i2);
        } else { // ???!!
            return dtotype.substring(i1 + 1);
        }
    }

    public String getDtoHintType() {
        String dtotype = getDtoType();
        if (dtotype == null) {
            String gp = getField() != null ? getField().getGenericParameter() : null;
            if (gp != null) {
                JAMDtoClass jc = JAMDtoGenerator.getJAMClass(gp);
                String cn;
                if (jc != null) {
                    cn = jc.getDtoPackageName() + "." + jc.getDtoClassName();
                } else {
                    cn = gp;
                }
                return cn;
            }
        } else if (dtotype.indexOf('<') > 0) {
            return extractGenericParameter(dtotype);
        }
        return null;
    }

    public String getHintType() {
        String gp = getField() != null ? getField().getGenericParameter() : null;
        if (gp != null) {
            return gp;
        }
        return null;
    }

    private String optimizeType(String type) {
        String myPackage = element.getDtoClass().getDtoPackageName() + ".";
        if (type == null) return type;
        if (type.startsWith("java.lang.")) {
            return type.substring("java.lang.".length());
        } else if (type.startsWith(myPackage)) {
            return type.substring(myPackage.length());
        } else {
            return type;
        }
    }

    public String getGetterName() {
        return getGetterName(getElement(), getDtoFieldType());
    }

    public static String getGetterName(JAMDtoAnnotatedElement el, String type) {
        if (type.equals("boolean")) {
            return "is" + StringUtils.capitalize(el.getName());
        } else {
            return "get" + StringUtils.capitalize(el.getName());
        }
    }

    /** @return true wenn mind. ein element im pfad nullable ist */
    public boolean isNullable() {
        JAMDtoAnnotatedElement[] elements = getTargetElements();
        for (JAMDtoAnnotatedElement each : elements) {
            String getter = getGetterName(each, each.getType());
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
            String getter = getGetterName(each, each.getType());
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
            String getter = getGetterName(field, field.getType());
            JAMDtoMethod meth = field.getDtoClass().getMethod(getter);
            if (meth == null) return null;
            JAMAnnotation anno = meth.getAnnotation("javax.persistence.Column");
            if (anno != null) {
                return anno.getIntValue("length");
            }
        }
        return null;
    }

    protected JAMDtoAnnotatedElement getTargetElement() {
        JAMDtoAnnotatedElement[] elements = getTargetElements();
        return elements[elements.length - 1];
    }

    protected JAMDtoAnnotatedElement[] getTargetElements() {
        if(targetElements != null) return targetElements;
        List<JAMDtoAnnotatedElement> elements = new ArrayList(2);
        elements.add(element);
        JAMDtoAnnotatedElement field = element;
        String path = getDtoPath();
        if (path != null) {
            int idx = path.lastIndexOf('.');
            if (idx <= 0) {
                field = new JAMDtoField(
                        JAMDtoAnnotatedElement.findField(field.getTypeJClass(), path),
                        new JAMDtoClass(field.getTypeJClass()));
                elements.add(field);
            } else {
                path = path.substring(0, idx);
                JField jf = JAMDtoAnnotatedElement
                        .findField(field.getDtoClass().getTypeJClass(), path);
                if (jf != null) {
                    field = new JAMDtoField(jf, field.getDtoClass());
                    elements.add(field);
                }
            }
        }
        targetElements = elements.toArray(new JAMDtoAnnotatedElement[elements.size()]);
        return targetElements;
    }

    public boolean isRelationship() {
        JAMDtoMethod meth = element.getDtoClass().getMethod(getGetterName());
        return meth != null && getDtoPath() == null && getDtoConverter() == null && (
                null != meth.getAnnotation("javax.persistence.OneToMany") ||
                        null != meth.getAnnotation("javax.persistence.ManyToOne") ||
                        null != meth.getAnnotation("javax.persistence.ManyToMany")||
                        null != meth.getAnnotation("javax.persistence.OneToOne"));
    }

    public String toString() {
        return element + ";" + annotation;
    }
}