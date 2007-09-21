//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.09.21 at 11:30:54 AM EDT 
//


package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlEnum;


/**
 * <p>Java class for Importance.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Importance">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="IRRELEVANT"/>
 *     &lt;enumeration value="LOW"/>
 *     &lt;enumeration value="MEDIUM"/>
 *     &lt;enumeration value="HIGH"/>
 *     &lt;enumeration value="CRITICAL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum Importance {

    IRRELEVANT,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public String value() {
        return name();
    }

    public static Importance fromValue(String v) {
        return valueOf(v);
    }

}
