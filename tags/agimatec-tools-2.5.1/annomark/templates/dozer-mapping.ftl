<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mappings PUBLIC "-//DOZER//DTD MAPPINGS//EN"
    "http://dozer.sourceforge.net/dtd/dozerbeanmapping.dtd">

<mappings>
<!--
  CODE GENERATED BY "${generator.templateName}" - DO NOT EDIT!
 -->
  <configuration>
    <stop-on-errors>true</stop-on-errors>
    <!-- default dateformat will apply to all class maps unless the class mapping explicitly overrides it -->
    <date-format>MM/dd/yyyy HH:mm</date-format>
    <!-- default wildcard policy that will apply to all class maps unless the class mapping explicitly overrides it -->
    <wildcard>true</wildcard>
  </configuration>

<#list classes?sort as class><#if class.dtoCurrentlyActive>

  <mapping>
    <class-a>${class.name}</class-a>
    <class-b>${class.dtoPackageName}.${class.dtoClassName}</class-b>
    <#list class.dtoFieldAnnotations as each><#if
    each.element.name!=each.dtoName || each.dtoConverter?? ||
    each.dtoPath?? || each.dtoCopyByReference ||
    each.element.enumType || each.dtoAddMethod?? || each.dtoOneWay>

    <field<#if
    each.dtoConverter??> custom-converter="${each.dtoConverter}"</#if><#if
    each.dtoOneWay> type="one-way"</#if><#if
    each.dtoCopyByReference || (each.element.enumType && !each.dtoConverter??)> copy-by-reference="true"</#if>>
        <a<#if each.dtoAddMethod??> set-method="${each.dtoAddMethod}" type="iterate"</#if>>${each.element.name}<#if each.dtoPath??>.${each.dtoPath}</#if></a>
        <b>${each.dtoName}</b><#if each.hintType?? && each.dtoBeanType??>
        <a-hint>${each.hintType}</a-hint>
        <b-hint>${each.dtoBeanType}</b-hint></#if>
    </field></#if></#list>
  </mapping>

</#if></#list>

</mappings>
